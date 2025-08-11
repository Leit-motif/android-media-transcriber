package com.audioscribe.app.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network client factory for creating Retrofit instances
 */
object NetworkClient {
    
    private const val TIMEOUT_SECONDS = 60L // Whisper API can take time for longer audio files
    
    /**
     * Create OkHttpClient with appropriate timeouts and logging
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Create Retrofit instance for Whisper API
     */
    fun createWhisperApiService(): WhisperApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(WhisperApiService.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(WhisperApiService::class.java)
    }
}
