package com.audioscribe.app.utils;

/**
 * API constants for the application
 * TODO: Move API keys to secure storage (Task #11: Implement API Key Security)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/audioscribe/app/utils/ApiConstants;", "", "()V", "OPENAI_API_KEY", "", "isApiKeyConfigured", "", "app_debug"})
public final class ApiConstants {
    
    /**
     * OpenAI API Key
     * IMPORTANT: This is a temporary hardcoded solution for proof of concept
     * In production, this should be:
     * 1. Stored in Android Keystore
     * 2. Retrieved from a secure backend
     * 3. Or loaded from local.properties (not committed to git)
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OPENAI_API_KEY = "YOUR_OPENAI_API_KEY_HERE";
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.utils.ApiConstants INSTANCE = null;
    
    private ApiConstants() {
        super();
    }
    
    /**
     * Check if API key is configured
     */
    public final boolean isApiKeyConfigured() {
        return false;
    }
}