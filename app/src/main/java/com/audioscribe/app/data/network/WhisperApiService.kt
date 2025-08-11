package com.audioscribe.app.data.network

import com.audioscribe.app.data.model.TranscriptionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit service interface for OpenAI Whisper API
 */
interface WhisperApiService {
    
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null,
        @Part("response_format") responseFormat: RequestBody? = null,
        @Part("temperature") temperature: RequestBody? = null
    ): Response<TranscriptionResponse>
    
    companion object {
        const val BASE_URL = "https://api.openai.com/v1/"
        const val DEFAULT_MODEL = "whisper-1"
        const val DEFAULT_RESPONSE_FORMAT = "json"
        const val DEFAULT_LANGUAGE = "en"
        
        /**
         * Create authorization header value
         */
        fun createAuthHeader(apiKey: String): String = "Bearer $apiKey"
        
        /**
         * Create request body for form data
         */
        fun createRequestBody(value: String): RequestBody = 
            RequestBody.create("text/plain".toMediaType(), value)
    }
}
