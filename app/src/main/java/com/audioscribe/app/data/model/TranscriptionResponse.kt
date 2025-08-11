package com.audioscribe.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for OpenAI Whisper API transcription
 */
data class TranscriptionResponse(
    @SerializedName("text")
    val text: String
)

/**
 * Error response model for API failures
 */
data class ApiErrorResponse(
    @SerializedName("error")
    val error: ApiError
)

data class ApiError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("code")
    val code: String? = null
)
