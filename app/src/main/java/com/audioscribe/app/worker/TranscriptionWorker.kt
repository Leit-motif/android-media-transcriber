package com.audioscribe.app.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import androidx.work.ListenableWorker
import com.audioscribe.app.data.repository.TranscriptionRepository
import com.audioscribe.app.service.AudioCaptureService
import com.audioscribe.app.utils.ApiKeyStore
import com.audioscribe.app.utils.FileManager
import com.audioscribe.app.data.repository.SessionRepository
import com.audioscribe.app.data.database.entity.TranscriptChunk
import com.audioscribe.app.data.database.entity.ChunkStatus
import java.util.Date
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException
import retrofit2.HttpException
import android.os.BatteryManager
import android.content.Context.BATTERY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context.CONNECTIVITY_SERVICE

/**
 * CoroutineWorker for handling transcription tasks in the background.
 * This worker processes audio files and sends transcription results via broadcast.
 */
class TranscriptionWorker(
	context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {

	companion object {
		private const val TAG = "TranscriptionWorker"
		
		// Input data keys
		const val KEY_AUDIO_FILE_PATH = "audio_file_path"
		const val KEY_LANGUAGE = "language"
		const val KEY_SESSION_ID = "session_id"
		const val KEY_CHUNK_ORDER = "chunk_order"
		
		// Output data keys
		const val KEY_RESULT_TEXT = "result_text"
		const val KEY_ERROR_MESSAGE = "error_message"
		
		// Retry configuration
		private const val MAX_RETRY_ATTEMPTS = 3
		
		/**
		 * Create input data for the transcription worker
		 */
		fun createInputData(
			audioFilePath: String, 
			language: String? = null,
			sessionId: Long? = null,
			chunkOrder: Int? = null
		): Data {
			val builder = Data.Builder()
				.putString(KEY_AUDIO_FILE_PATH, audioFilePath)
			
			// Only add language if it's not null (for auto-detection, omit the parameter)
			if (language != null) {
				builder.putString(KEY_LANGUAGE, language)
			}
			
			// Add session information if provided
			sessionId?.let { builder.putLong(KEY_SESSION_ID, it) }
			chunkOrder?.let { builder.putInt(KEY_CHUNK_ORDER, it) }
			
			return builder.build()
		}
	}

	private val transcriptionRepository = TranscriptionRepository()
	private val sessionRepository by lazy { SessionRepository(applicationContext) }

	override suspend fun doWork(): Result {
		return try {
			Log.d(TAG, "Starting transcription work (attempt ${runAttemptCount + 1})")
			
			// Check if work has been cancelled before starting
			if (isStopped) {
				Log.i(TAG, "Work was cancelled before starting")
				return Result.failure(createErrorOutput("Work was cancelled"))
			}
			
			// Get input parameters
			val audioFilePath = inputData.getString(KEY_AUDIO_FILE_PATH)
			val language = inputData.getString(KEY_LANGUAGE) // null means auto-detect
			val sessionId = if (inputData.hasKeyWithValueOfType(KEY_SESSION_ID, Long::class.java)) {
				inputData.getLong(KEY_SESSION_ID, -1L).takeIf { it != -1L }
			} else null
			val chunkOrder = if (inputData.hasKeyWithValueOfType(KEY_CHUNK_ORDER, Int::class.java)) {
				inputData.getInt(KEY_CHUNK_ORDER, -1).takeIf { it != -1 }
			} else null
			
			if (audioFilePath.isNullOrBlank()) {
				Log.e(TAG, "Audio file path is null or empty")
				return Result.failure(createErrorOutput("Audio file path is required"))
			}
			
			val audioFile = File(audioFilePath)
			if (!audioFile.exists()) {
				Log.e(TAG, "Audio file does not exist: $audioFilePath")
				return Result.failure(createErrorOutput("Audio file not found: $audioFilePath"))
			}
			
			// Check cancellation again before API key retrieval
			if (isStopped) {
				Log.i(TAG, "Work was cancelled during validation")
				return Result.failure(createErrorOutput("Work was cancelled"))
			}
			
			// Get API key
			val apiKey = ApiKeyStore.getApiKey(applicationContext)
			if (apiKey.isBlank()) {
				Log.e(TAG, "API key not configured")
				val errorMessage = "OpenAI API key not configured. Set it in Settings."
				sendTranscriptionBroadcast(error = errorMessage)
				return Result.failure(createErrorOutput(errorMessage))
			}
			
			// Check device constraints before proceeding with expensive operation
			if (!checkDeviceConstraints()) {
				Log.w(TAG, "Device constraints not met, deferring work")
				return Result.retry()
			}
			
			Log.d(TAG, "Transcribing file: ${audioFile.name} (${audioFile.length()} bytes)")
			
			// Check cancellation one more time before starting the network call
			if (isStopped) {
				Log.i(TAG, "Work was cancelled before transcription")
				return Result.failure(createErrorOutput("Work was cancelled"))
			}
			
			// Perform transcription
			val result = transcriptionRepository.transcribeAudio(audioFile, apiKey, language)
			
			result.onSuccess { transcriptionText ->
				Log.i(TAG, "Transcription completed successfully on attempt ${runAttemptCount + 1}")
				Log.d(TAG, "Transcription text: ${transcriptionText.take(100)}...")
				
				// Store chunk in database if session information is available
				if (sessionId != null && chunkOrder != null) {
					try {
						// Compute duration from file size (subtract WAV header ~44 bytes)
						val bytesPerSecond = 44100 * 2 * 2 // sampleRate * channels(2) * bytesPerSample(2)
						val dataBytes = (audioFile.length() - 44).coerceAtLeast(0)
						val durationMs = ((dataBytes * 1000) / bytesPerSecond).toLong()
						
						val chunk = TranscriptChunk(
							sessionId = sessionId,
							chunkIndex = chunkOrder,
							text = transcriptionText,
							durationMs = durationMs,
							audioFileSizeBytes = audioFile.length(),
							originalFileName = audioFile.name,
							status = ChunkStatus.COMPLETED,
							transcriptionCompletedAt = Date()
						)
						sessionRepository.addChunkToSession(chunk)
						Log.d(TAG, "Stored chunk $chunkOrder for session $sessionId in database")
					} catch (e: Exception) {
						Log.e(TAG, "Failed to store chunk in database", e)
						// Don't fail the work just because database storage failed
					}
				} else {
					Log.d(TAG, "No session information available, skipping database storage")
				}
				
				// Delete the WAV file after successful transcription
				val fileDeleted = FileManager.deleteTranscribedFile(audioFile)
				if (fileDeleted) {
					Log.d(TAG, "WAV file deleted after successful transcription: ${audioFile.name}")
				} else {
					Log.w(TAG, "Failed to delete WAV file after transcription: ${audioFile.name}")
				}
				
				// Send success broadcast
				sendTranscriptionBroadcast(text = transcriptionText)
				
				// Return success with result data
				val outputData = Data.Builder()
					.putString(KEY_RESULT_TEXT, transcriptionText)
					.build()
				
				return Result.success(outputData)
				
			}.onFailure { error ->
				Log.e(TAG, "Transcription failed on attempt ${runAttemptCount + 1}: ${error.message}", error)
				
				// Determine if this error should trigger a retry
				val shouldRetry = shouldRetryForError(error)
				
				if (shouldRetry && runAttemptCount < MAX_RETRY_ATTEMPTS) {
					Log.w(TAG, "Retryable error encountered, will retry. Attempt ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS")
					return Result.retry()
				} else {
					Log.e(TAG, "Non-retryable error or max attempts reached. Failing work.")
					val errorMessage = error.message ?: "Unknown transcription error"
					
					// Send error broadcast
					sendTranscriptionBroadcast(error = errorMessage)
					
					return Result.failure(createErrorOutput(errorMessage))
				}
			}
			
			// This should never be reached due to the onSuccess/onFailure blocks above
			Result.failure(createErrorOutput("Unexpected transcription result"))
			
		} catch (e: Exception) {
			Log.e(TAG, "Unexpected error during transcription work on attempt ${runAttemptCount + 1}", e)
			
			// Determine if this exception should trigger a retry
			val shouldRetry = shouldRetryForError(e)
			
			if (shouldRetry && runAttemptCount < MAX_RETRY_ATTEMPTS) {
				Log.w(TAG, "Retryable exception encountered, will retry. Attempt ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS")
				return Result.retry()
			} else {
				Log.e(TAG, "Non-retryable exception or max attempts reached. Failing work.")
				val errorMessage = e.message ?: "Unexpected error during transcription"
				
				// Send error broadcast
				sendTranscriptionBroadcast(error = errorMessage)
				
				return Result.failure(createErrorOutput(errorMessage))
			}
		}
	}
	
	/**
	 * Check if device constraints are met for transcription work
	 */
	private fun checkDeviceConstraints(): Boolean {
		// Check battery level
		if (!checkBatteryConstraints()) {
			Log.d(TAG, "Battery constraints not met")
			return false
		}
		
		// Check network connectivity
		if (!checkNetworkConstraints()) {
			Log.d(TAG, "Network constraints not met")
			return false
		}
		
		Log.d(TAG, "All device constraints are met")
		return true
	}
	
	/**
	 * Check battery-related constraints
	 */
	private fun checkBatteryConstraints(): Boolean {
		val batteryManager = applicationContext.getSystemService(BATTERY_SERVICE) as? BatteryManager
			?: return true // If we can't check, assume it's okay
		
		// Check if battery is low (below 15%)
		val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
		val isLowBattery = batteryLevel < 15
		
		if (isLowBattery) {
			Log.d(TAG, "Battery is low ($batteryLevel%), deferring transcription")
			return false
		}
		
		Log.d(TAG, "Battery level is acceptable ($batteryLevel%)")
		return true
	}
	
	/**
	 * Check network-related constraints
	 */
	private fun checkNetworkConstraints(): Boolean {
		val connectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
			?: return false // If we can't check connectivity, fail safe
		
		val network = connectivityManager.activeNetwork ?: return false
		val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
		
		// Check if we have internet connectivity
		val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
						 capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
		
		if (!hasInternet) {
			Log.d(TAG, "No internet connectivity available")
			return false
		}
		
		Log.d(TAG, "Network connectivity is available")
		return true
	}
	
	/**
	 * Create error output data
	 */
	private fun createErrorOutput(errorMessage: String): Data {
		return Data.Builder()
			.putString(KEY_ERROR_MESSAGE, errorMessage)
			.build()
	}
	
	/**
	 * Send transcription result broadcast to update the UI
	 */
	private fun sendTranscriptionBroadcast(text: String? = null, error: String? = null) {
		val intent = Intent(AudioCaptureService.ACTION_TRANSCRIPTION_COMPLETE).apply {
			text?.let { putExtra(AudioCaptureService.EXTRA_TRANSCRIPTION_TEXT, it) }
			error?.let { putExtra(AudioCaptureService.EXTRA_TRANSCRIPTION_ERROR, it) }
		}
		applicationContext.sendBroadcast(intent)
	}
	
	/**
	 * Determine if an error should trigger a retry based on its type
	 */
	private fun shouldRetryForError(error: Throwable): Boolean {
		return when (error) {
			// Network-related errors that are likely transient
			is SocketTimeoutException -> {
				Log.d(TAG, "Socket timeout - retryable")
				true
			}
			is UnknownHostException -> {
				Log.d(TAG, "Unknown host - retryable (network issue)")
				true
			}
			is IOException -> {
				Log.d(TAG, "IO exception - retryable")
				true
			}
			
			// HTTP errors - some are retryable, others are not
			is HttpException -> {
				when (error.code()) {
					// Server errors (5xx) - likely transient, should retry
					in 500..599 -> {
						Log.d(TAG, "Server error ${error.code()} - retryable")
						true
					}
					
					// Rate limiting (429) - should retry with backoff
					429 -> {
						Log.d(TAG, "Rate limited (429) - retryable")
						true
					}
					
					// Client errors (4xx) - usually not retryable
					400 -> {
						Log.d(TAG, "Bad request (400) - not retryable")
						false
					}
					401 -> {
						Log.d(TAG, "Unauthorized (401) - not retryable (API key issue)")
						false
					}
					403 -> {
						Log.d(TAG, "Forbidden (403) - not retryable (permission issue)")
						false
					}
					404 -> {
						Log.d(TAG, "Not found (404) - not retryable")
						false
					}
					413 -> {
						Log.d(TAG, "Payload too large (413) - not retryable (file too big)")
						false
					}
					
					// Other 4xx errors - generally not retryable
					in 400..499 -> {
						Log.d(TAG, "Client error ${error.code()} - not retryable")
						false
					}
					
					// Other errors - retry to be safe
					else -> {
						Log.d(TAG, "HTTP error ${error.code()} - retryable")
						true
					}
				}
			}
			
			// Security exceptions (API key issues) - not retryable
			is SecurityException -> {
				Log.d(TAG, "Security exception - not retryable")
				false
			}
			
			// File not found or access issues - not retryable
			is java.io.FileNotFoundException -> {
				Log.d(TAG, "File not found - not retryable")
				false
			}
			
			// Other exceptions - retry to be safe
			else -> {
				Log.d(TAG, "Unknown error type ${error::class.simpleName} - retryable")
				true
			}
		}
	}
}
