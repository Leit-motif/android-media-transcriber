package com.audioscribe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.audioscribe.app.R
import com.audioscribe.app.data.repository.TranscriptionRepository
import com.audioscribe.app.utils.ApiKeyStore
import com.audioscribe.app.ui.MainActivity
import com.audioscribe.app.worker.TranscriptionWorker
import com.audioscribe.app.utils.WorkManagerConfig
import com.audioscribe.app.data.repository.SessionRepository
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.SessionStatus
import com.audioscribe.app.data.database.entity.TranscriptChunk
import java.util.Date
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Foreground service for capturing system audio
 * Uses MediaProjection API with AudioPlaybackCapture to capture audio from other apps
 */
@RequiresApi(Build.VERSION_CODES.Q)
class AudioCaptureService : LifecycleService() {
    
    companion object {
        private const val TAG = "AudioCaptureService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "audio_capture_channel"
        private const val CHANNEL_NAME = "Audio Capture"
        
        // Audio configuration constants
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
		private const val CHUNK_SECONDS = 30 // Fixed-length chunk duration (demo-friendly)
        
        // Intent extras
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
			
			// Transcription broadcast
			const val ACTION_TRANSCRIPTION_COMPLETE = "ACTION_TRANSCRIPTION_COMPLETE"
			const val EXTRA_TRANSCRIPTION_TEXT = "EXTRA_TRANSCRIPTION_TEXT"
			const val EXTRA_TRANSCRIPTION_ERROR = "EXTRA_TRANSCRIPTION_ERROR"
        
        // Actions
        const val ACTION_START_CAPTURE = "ACTION_START_CAPTURE"
        const val ACTION_STOP_CAPTURE = "ACTION_STOP_CAPTURE"
    }
    
    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var inFlightTranscriptions = 0
    
    private var outputFile: File? = null
    private val transcriptionRepository = TranscriptionRepository()
    private val sessionRepository by lazy { SessionRepository(this) }
    
    // Session management
    private var currentSessionId: Long? = null
    private var sessionStartTime: Date? = null
    private var chunkCounter = 0
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // CRITICAL: Start foreground immediately to prevent ForegroundServiceDidNotStartInTimeException
        startForeground(NOTIFICATION_ID, createNotification(isRecording = false, isMicrophoneMode = false))
        Log.d(TAG, "AudioCaptureService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                
                if (resultCode != 0 && resultData != null) {
                    // Try system audio capture first
                    startAudioCapture(resultCode, resultData)
                } else {
                    // Fallback to microphone recording
                    Log.i(TAG, "No MediaProjection data provided, using microphone recording")
                    startMicrophoneRecording()
                }
            }
            ACTION_STOP_CAPTURE -> {
                stopAudioCapture()
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent?.action}")
            }
        }
        
        return START_STICKY
    }
    
    private fun startAudioCapture(resultCode: Int, resultData: Intent) {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }
        
        try {
            // Check API level
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Log.e(TAG, "AudioPlaybackCapture requires Android 10+ (API 29+), current: ${Build.VERSION.SDK_INT}")
                Log.i(TAG, "Falling back to microphone recording")
                startMicrophoneRecording()
                return
            }
            
            Log.d(TAG, "Starting audio capture on API ${Build.VERSION.SDK_INT}")
            
            // Initialize MediaProjection
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
            
            if (mediaProjection == null) {
                Log.e(TAG, "Failed to create MediaProjection")
                Log.i(TAG, "Falling back to microphone recording")
                startMicrophoneRecording()
                return
            }
            
            // Create output file
            createOutputFile()
            
            // Set up AudioPlaybackCaptureConfiguration
            Log.d(TAG, "Creating AudioPlaybackCaptureConfiguration")
            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()
                
            Log.d(TAG, "AudioPlaybackCaptureConfiguration created successfully")
            
            // Calculate buffer size
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size: $bufferSize")
                Log.i(TAG, "Falling back to microphone recording")
                startMicrophoneRecording()
                return
            }
            
            // Create AudioRecord with playback capture
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AUDIO_FORMAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .build()
            
            Log.d(TAG, "Creating AudioRecord with buffer size: ${bufferSize * 2}")
            
            audioRecord = AudioRecord.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize * 2) // Double buffer for safety
                .setAudioPlaybackCaptureConfig(config)
                .build()
            
            val recordState = audioRecord?.state
            Log.d(TAG, "AudioRecord state: $recordState")
            
            if (recordState != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed. State: $recordState")
                Log.e(TAG, "Expected: ${AudioRecord.STATE_INITIALIZED}")
                Log.i(TAG, "Falling back to microphone recording")
                startMicrophoneRecording()
                return
            }
            
            // Update notification to show recording status
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Capture")
                .setContentText("Recording system audio...")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            
			// Create session in database
			sessionStartTime = Date()
			// Create session asynchronously but ensure it's ready before recording
			serviceScope.launch {
				try {
					val session = TranscriptionSession(
						startTime = sessionStartTime!!,
						endTime = null,
						title = "Session ${System.currentTimeMillis()}",
						status = SessionStatus.IN_PROGRESS
					)
					currentSessionId = sessionRepository.startNewSession(session)
					chunkCounter = 0
					Log.d(TAG, "Created session in database with ID: $currentSessionId")
					Log.d(TAG, "Session creation completed, chunkCounter initialized to: $chunkCounter")
				} catch (e: Exception) {
					Log.e(TAG, "Failed to create session in database", e)
				}
			}
			
			// Wait for session creation to complete (simple polling)
			var attempts = 0
			while (currentSessionId == null && attempts < 50) { // Wait up to 5 seconds
				Thread.sleep(100)
				attempts++
			}
			if (currentSessionId == null) {
				Log.w(TAG, "Session creation timeout, proceeding without session")
			}
            
            // Start recording
            audioRecord?.startRecording()
            isRecording = true
            
            // Start recording coroutine
            recordingJob = serviceScope.launch {
                recordAudio()
            }
            
            Log.i(TAG, "Audio capture started successfully")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception - MediaProjection permission denied or AudioPlaybackCapture not supported", e)
            Log.i(TAG, "Falling back to microphone recording")
            startMicrophoneRecording()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio capture", e)
            Log.i(TAG, "Falling back to microphone recording")
            startMicrophoneRecording()
        }
    }
    
    private fun stopAudioCapture() {
        if (!isRecording) {
            Log.w(TAG, "Not recording")
            return
        }
        
        // Signal the recording loop to exit and let it finalize the last chunk
        isRecording = false
        
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            
            mediaProjection?.stop()
            mediaProjection = null
            
            // Update session end time
            currentSessionId?.let { sessionId ->
                serviceScope.launch {
                    try {
                        val session = sessionRepository.getSession(sessionId)
                        if (session != null) {
                            val updatedSession = session.copy(
                                endTime = Date(),
                                status = SessionStatus.COMPLETED
                            )
                            sessionRepository.updateSession(updatedSession)
                            Log.d(TAG, "Updated session $sessionId as completed")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update session end time", e)
                    }
                }
            }
            
            Log.i(TAG, "Audio capture stopped (awaiting chunk finalization/transcription if any)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio capture", e)
        }
        
        // When the recording job completes and no transcriptions are in flight, stop the service
        recordingJob?.invokeOnCompletion { maybeStopService() }
        maybeStopService()
    }

    private fun maybeStopService() {
        if (!isRecording && (recordingJob?.isActive != true) && inFlightTranscriptions == 0) {
            try {
                stopForeground(true)
                stopSelf()
            } catch (_: Exception) { }
        }
    }
    
	private suspend fun recordAudio() = withContext(Dispatchers.IO) {
		val audioRecord = this@AudioCaptureService.audioRecord ?: return@withContext
		
		val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
		val audioBuffer = ByteArray(bufferSize)
		val bytesPerSecond = SAMPLE_RATE * 2 * 2 // sampleRate * channels(2) * bytesPerSample(2)
		val maxChunkBytes = bytesPerSecond * CHUNK_SECONDS
		
		var currentFile: File? = null
		var fos: FileOutputStream? = null
		var chunkBytesWritten = 0
		var chunkStartMillis: Long = 0L
		
		fun openNewChunk() {
			try {
				// Always create a fresh file per chunk
				createOutputFile()
				currentFile = this@AudioCaptureService.outputFile
				fos = FileOutputStream(currentFile!!)
				writeWavHeader(fos!!, 0)
				chunkBytesWritten = 0
				chunkStartMillis = System.currentTimeMillis()
				Log.d(TAG, "Opened new chunk: ${currentFile?.name}")
			} catch (e: Exception) {
				Log.e(TAG, "Failed to open new chunk", e)
				throw e
			}
		}
		
		fun closeAndProcessCurrentChunk() {
			try {
				fos?.close()
				val file = currentFile
				if (file != null) {
					if (chunkBytesWritten > 0) {
						updateWavHeader(file, chunkBytesWritten)
						Log.i(TAG, "Chunk saved: ${file.absolutePath}, size: $chunkBytesWritten bytes")
						startTranscription(file)
					} else {
						// Empty chunk (e.g., stopped immediately). Clean up the placeholder file.
						if (file.exists()) file.delete()
						Log.d(TAG, "Deleted empty chunk: ${file.absolutePath}")
					}
				}
			} catch (e: Exception) {
				Log.e(TAG, "Error closing/processing chunk", e)
			}
		}
		
		try {
			// Initialize first chunk
			if (this@AudioCaptureService.outputFile == null) {
				createOutputFile()
			}
			openNewChunk()
			
			while (isRecording) {
				val bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.size)
				if (bytesRead > 0) {
					fos?.write(audioBuffer, 0, bytesRead)
					chunkBytesWritten += bytesRead
					val elapsedMs = System.currentTimeMillis() - chunkStartMillis
					if (chunkBytesWritten >= maxChunkBytes || elapsedMs >= CHUNK_SECONDS * 1000) {
						// Rotate file
						closeAndProcessCurrentChunk()
						openNewChunk()
					}
				} else if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
					Log.e(TAG, "AudioRecord read error: ERROR_INVALID_OPERATION")
					break
				} else if (bytesRead == AudioRecord.ERROR_BAD_VALUE) {
					Log.e(TAG, "AudioRecord read error: ERROR_BAD_VALUE")
					break
				}
			}
			
			// Finalize any remaining bytes in the last chunk
			closeAndProcessCurrentChunk()
		} catch (e: IOException) {
			Log.e(TAG, "Error writing audio data", e)
		} finally {
			try { fos?.close() } catch (_: Exception) {}
		}
	}
    
    private fun createOutputFile() {
        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "audioscribe_$timestamp.wav"
            val outputDir = getExternalFilesDir(null) ?: filesDir
            
            // Ensure directory exists
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            outputFile = File(outputDir, fileName)
            Log.d(TAG, "Output file created: ${outputFile?.absolutePath}")
            
            // Test if we can write to the file
            outputFile?.let { file ->
                val parentFile = file.parentFile
                if (parentFile != null && !parentFile.canWrite()) {
                    Log.e(TAG, "Cannot write to output directory: ${parentFile.absolutePath}")
                    throw SecurityException("Cannot write to output directory")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create output file", e)
            throw e
        }
    }
    
    private fun writeWavHeader(fos: FileOutputStream, dataSize: Int) {
        val header = ByteArray(44)
        val totalSize = dataSize + 36
        val byteRate = SAMPLE_RATE * 2 * 2 // SampleRate * NumChannels * BitsPerSample/8
        
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        // File size
        header[4] = (totalSize and 0xff).toByte()
        header[5] = ((totalSize shr 8) and 0xff).toByte()
        header[6] = ((totalSize shr 16) and 0xff).toByte()
        header[7] = ((totalSize shr 24) and 0xff).toByte()
        
        // WAVE header
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        // Subchunk1Size (16 for PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        
        // AudioFormat (1 for PCM)
        header[20] = 1
        header[21] = 0
        
        // NumChannels (2 for stereo)
        header[22] = 2
        header[23] = 0
        
        // SampleRate
        header[24] = (SAMPLE_RATE and 0xff).toByte()
        header[25] = ((SAMPLE_RATE shr 8) and 0xff).toByte()
        header[26] = ((SAMPLE_RATE shr 16) and 0xff).toByte()
        header[27] = ((SAMPLE_RATE shr 24) and 0xff).toByte()
        
        // ByteRate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        
        // BlockAlign
        header[32] = 4 // NumChannels * BitsPerSample/8
        header[33] = 0
        
        // BitsPerSample
        header[34] = 16
        header[35] = 0
        
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        // Subchunk2Size (data size)
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()
        
        fos.write(header)
    }
    
    private fun updateWavHeader(file: File, dataSize: Int) {
        try {
            val raf = java.io.RandomAccessFile(file, "rw")
            val totalSize = dataSize + 36
            
            // Update file size at offset 4
            raf.seek(4)
            raf.write(totalSize and 0xff)
            raf.write((totalSize shr 8) and 0xff)
            raf.write((totalSize shr 16) and 0xff)
            raf.write((totalSize shr 24) and 0xff)
            
            // Update data size at offset 40
            raf.seek(40)
            raf.write(dataSize and 0xff)
            raf.write((dataSize shr 8) and 0xff)
            raf.write((dataSize shr 16) and 0xff)
            raf.write((dataSize shr 24) and 0xff)
            
            raf.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error updating WAV header", e)
        }
    }
    
    override fun onBind(intent: Intent): IBinder? = null
    
    private fun startMicrophoneRecording() {
        try {
            Log.i(TAG, "Starting microphone recording as fallback")
            
            // Create output file
            createOutputFile()
            
            // Calculate buffer size
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size for microphone: $bufferSize")
                showErrorToast("Failed to initialize microphone recording")
                return
            }
            
            // Create AudioRecord for microphone
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AUDIO_FORMAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .build()
            
            audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize * 2)
                .build()
            
            val recordState = audioRecord?.state
            Log.d(TAG, "Microphone AudioRecord state: $recordState")
            
            if (recordState != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Microphone AudioRecord initialization failed. State: $recordState")
                showErrorToast("Failed to initialize microphone")
                return
            }
            
            // Update notification to show microphone recording
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Capture")
                .setContentText("Recording from microphone...")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            
			// Create session in database
			sessionStartTime = Date()
			// Create session asynchronously but ensure it's ready before recording
			serviceScope.launch {
				try {
					val session = TranscriptionSession(
						startTime = sessionStartTime!!,
						endTime = null,
						title = "Session ${System.currentTimeMillis()}",
						status = SessionStatus.IN_PROGRESS
					)
					currentSessionId = sessionRepository.startNewSession(session)
					chunkCounter = 0
					Log.d(TAG, "Created microphone session in database with ID: $currentSessionId")
					Log.d(TAG, "Microphone session creation completed, chunkCounter initialized to: $chunkCounter")
				} catch (e: Exception) {
					Log.e(TAG, "Failed to create microphone session in database", e)
				}
			}
			
			// Wait for session creation to complete (simple polling)
			var attempts = 0
			while (currentSessionId == null && attempts < 50) { // Wait up to 5 seconds
				Thread.sleep(100)
				attempts++
			}
			if (currentSessionId == null) {
				Log.w(TAG, "Microphone session creation timeout, proceeding without session")
			}
            
            // Start recording
            audioRecord?.startRecording()
            isRecording = true
            
            // Start recording coroutine
            recordingJob = serviceScope.launch {
                recordAudio()
            }
            
            Log.i(TAG, "Microphone recording started successfully")
            showErrorToast("Using microphone instead of system audio")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start microphone recording", e)
            showErrorToast("Recording failed: ${e.message}")
        }
    }

    private fun showErrorToast(message: String) {
        // Post to main thread to show toast
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAudioCapture()
        Log.d(TAG, "AudioCaptureService destroyed")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when Audioscribe is capturing audio"
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(isRecording: Boolean = false, isMicrophoneMode: Boolean = false): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, AudioCaptureService::class.java).apply {
            action = ACTION_STOP_CAPTURE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audioscribe")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
        
        if (isRecording) {
            val contentText = if (isMicrophoneMode) "Recording from microphone..." else "Recording audio..."
            builder.setContentText(contentText)
                .addAction(R.drawable.ic_notification, "Stop", stopPendingIntent)
        } else {
            builder.setContentText("Starting audio capture...")
        }
        
        return builder.build()
    }
    
    /**
     * Start transcription process for the recorded audio file using WorkManager
     */
    private fun startTranscription(audioFile: File) {
        try {
            Log.d(TAG, "Enqueuing transcription work for file: ${audioFile.name}")
            
            // Update notification to show transcription in progress
            updateNotificationForTranscription(isProcessing = true)
            
            // Check if API key is configured before enqueuing work
            val apiKey = ApiKeyStore.getApiKey(this@AudioCaptureService)
            if (apiKey.isBlank()) {
                updateNotificationForTranscription(isProcessing = false, hasResult = false)
                sendTranscriptionBroadcast(error = "OpenAI API key not configured. Set it in Settings.")
                return
            }
            
            // Get constraints from configuration
            val constraints = WorkManagerConfig.getTranscriptionConstraints(this)
            
            // Create input data for the worker, including session information
            val inputData = TranscriptionWorker.createInputData(
                audioFilePath = audioFile.absolutePath,
                sessionId = currentSessionId,
                chunkOrder = chunkCounter++
                // language omitted for auto-detection
            )
            
            Log.d(TAG, "Creating transcription worker with sessionId: $currentSessionId, chunkOrder: ${chunkCounter - 1}")
            
            // Create and enqueue the work request
            val transcriptionWork = OneTimeWorkRequestBuilder<TranscriptionWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    WorkManagerConfig.getBackoffPolicy(),
                    WorkManagerConfig.getInitialBackoffDelaySeconds(),
                    WorkManagerConfig.getBackoffTimeUnit()
                )
                .build()
            
            // Track in-flight transcriptions for service lifecycle management
            inFlightTranscriptions++
            
            // Enqueue the work
            WorkManager.getInstance(this).enqueue(transcriptionWork)
            
            // Track work completion using coroutines instead of LiveData observers
            // This avoids lifecycle issues with Service context
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Poll for work completion
                    val workManager = WorkManager.getInstance(this@AudioCaptureService)
                    var workInfo = workManager.getWorkInfoById(transcriptionWork.id).get()
                    
                    while (workInfo != null && !workInfo.state.isFinished) {
                        delay(500) // Check every 500ms
                        workInfo = workManager.getWorkInfoById(transcriptionWork.id).get()
                    }
                    
                    // Work completed (success or failure)
                    inFlightTranscriptions = (inFlightTranscriptions - 1).coerceAtLeast(0)
                    
                    Log.d(TAG, "Transcription work completed with state: ${workInfo?.state}")
                    
                    // The worker handles broadcasting results, but we update notification here
                    when (workInfo?.state) {
                        androidx.work.WorkInfo.State.SUCCEEDED -> {
                            updateNotificationForTranscription(isProcessing = false, hasResult = true)
                        }
                        androidx.work.WorkInfo.State.FAILED -> {
                            updateNotificationForTranscription(isProcessing = false, hasResult = false)
                        }
                        else -> {
                            // Other states (CANCELLED, etc.)
                            updateNotificationForTranscription(isProcessing = false, hasResult = false)
                        }
                    }
                    
                    maybeStopService()
                } catch (e: Exception) {
                    Log.e(TAG, "Error monitoring transcription work", e)
                    inFlightTranscriptions = (inFlightTranscriptions - 1).coerceAtLeast(0)
                    updateNotificationForTranscription(isProcessing = false, hasResult = false)
                    maybeStopService()
                }
            }
            
            Log.i(TAG, "Transcription work enqueued successfully for file: ${audioFile.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error enqueuing transcription work", e)
            updateNotificationForTranscription(isProcessing = false, hasResult = false)
            sendTranscriptionBroadcast(error = e.message ?: "Failed to start transcription")
        }
    }
    
    /**
     * Update notification to show transcription status
     */
    private fun updateNotificationForTranscription(isProcessing: Boolean, hasResult: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = when {
            isProcessing -> "Transcribing audio..."
            hasResult -> "Transcription completed - Tap to view"
            else -> "Transcription failed"
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audioscribe")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(false) // Allow dismissal after transcription
            .setSilent(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Show transcription result to user
     */
		private fun showTranscriptionResult(transcriptionText: String) {
			// Kept for potential future UI cues from service; prefer broadcast to Activity
			Handler(Looper.getMainLooper()).post {
				val preview = if (transcriptionText.length > 100) {
					"${transcriptionText.take(100)}..."
				} else {
					transcriptionText
				}
				Toast.makeText(this, "Transcription: $preview", Toast.LENGTH_LONG).show()
			}
			Log.i(TAG, "Full transcription result: $transcriptionText")
		}

		private fun sendTranscriptionBroadcast(text: String? = null, error: String? = null) {
			val intent = Intent(ACTION_TRANSCRIPTION_COMPLETE).apply {
				text?.let { putExtra(EXTRA_TRANSCRIPTION_TEXT, it) }
				error?.let { putExtra(EXTRA_TRANSCRIPTION_ERROR, it) }
			}
			sendBroadcast(intent)
		}
}
