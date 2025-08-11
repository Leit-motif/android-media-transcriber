package com.audioscribe.app.data.network;

/**
 * Network client factory for creating Retrofit instances
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0002J\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/audioscribe/app/data/network/NetworkClient;", "", "()V", "TIMEOUT_SECONDS", "", "createOkHttpClient", "Lokhttp3/OkHttpClient;", "createWhisperApiService", "Lcom/audioscribe/app/data/network/WhisperApiService;", "app_debug"})
public final class NetworkClient {
    private static final long TIMEOUT_SECONDS = 60L;
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.data.network.NetworkClient INSTANCE = null;
    
    private NetworkClient() {
        super();
    }
    
    /**
     * Create OkHttpClient with appropriate timeouts and logging
     */
    private final okhttp3.OkHttpClient createOkHttpClient() {
        return null;
    }
    
    /**
     * Create Retrofit instance for Whisper API
     */
    @org.jetbrains.annotations.NotNull()
    public final com.audioscribe.app.data.network.WhisperApiService createWhisperApiService() {
        return null;
    }
}