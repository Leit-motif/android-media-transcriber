package com.audioscribe.app.data.repository

import android.util.Log
import com.audioscribe.app.data.network.NetworkClient
import com.audioscribe.app.data.network.WhisperApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * Repository for handling audio transcription operations
 */
class TranscriptionRepository {
    
    private val whisperApiService = NetworkClient.createWhisperApiService()
    
    companion object {
        private const val TAG = "TranscriptionRepository"
    }
    
    /**
     * Transcribe an audio file using OpenAI Whisper API
     * 
     * @param audioFile The audio file to transcribe
     * @param language Optional language code (default: "en")
     * @return Result containing transcription text or error
     */
    suspend fun transcribeAudio(
        audioFile: File,
        apiKey: String,
        language: String = WhisperApiService.DEFAULT_LANGUAGE
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting transcription for file: ${audioFile.name}, size: ${audioFile.length()} bytes")
            
            // Check apiKey
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("OpenAI API key not configured. Please set it in Settings."))
            }
            
            // Validate file
            if (!audioFile.exists() || audioFile.length() == 0L) {
                return@withContext Result.failure(Exception("Audio file does not exist or is empty"))
            }
            
            // Check file size (Whisper API has 25MB limit)
            val maxSizeBytes = 25 * 1024 * 1024 // 25MB
            if (audioFile.length() > maxSizeBytes) {
                return@withContext Result.failure(Exception("Audio file too large. Maximum size is 25MB"))
            }
            
            // Create multipart request
            val requestFile = RequestBody.create(
                "audio/wav".toMediaType(),
                audioFile
            )
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            
            // Create form data parts
            val modelPart = WhisperApiService.createRequestBody(WhisperApiService.DEFAULT_MODEL)
            val languagePart = WhisperApiService.createRequestBody(language)
            val responseFormatPart = WhisperApiService.createRequestBody(WhisperApiService.DEFAULT_RESPONSE_FORMAT)
            
            // Make API call
            val authHeader = WhisperApiService.createAuthHeader(apiKey)
            val response = whisperApiService.transcribeAudio(
                authorization = authHeader,
                file = filePart,
                model = modelPart,
                language = languagePart,
                responseFormat = responseFormatPart
            )
            
            if (response.isSuccessful) {
                val transcriptionResponse = response.body()
                if (transcriptionResponse != null && transcriptionResponse.text.isNotEmpty()) {
                    Log.d(TAG, "Transcription successful: ${transcriptionResponse.text.take(100)}...")
                    Result.success(transcriptionResponse.text)
                } else {
                    Result.failure(Exception("Empty transcription response"))
                }
            } else {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Transcription failed", e)
            Result.failure(e)
        }
    }
}
