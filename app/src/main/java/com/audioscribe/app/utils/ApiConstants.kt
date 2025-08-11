package com.audioscribe.app.utils

/**
 * API constants for the application
 * TODO: Move API keys to secure storage (Task #11: Implement API Key Security)
 */
object ApiConstants {
    
    /**
     * OpenAI API Key
     * IMPORTANT: This is a temporary hardcoded solution for proof of concept
     * In production, this should be:
     * 1. Stored in Android Keystore
     * 2. Retrieved from a secure backend
     * 3. Or loaded from local.properties (not committed to git)
     */
    const val OPENAI_API_KEY = "YOUR_OPENAI_API_KEY_HERE"
    
    /**
     * Check if API key is configured
     */
    fun isApiKeyConfigured(): Boolean {
        return OPENAI_API_KEY != "YOUR_OPENAI_API_KEY_HERE" && OPENAI_API_KEY.isNotBlank()
    }
}
