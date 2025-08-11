package com.audioscribe.app.data.network;

/**
 * Retrofit service interface for OpenAI Whisper API
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\bf\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fJV\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\t\u001a\u00020\n2\n\b\u0003\u0010\u000b\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\f\u001a\u0004\u0018\u00010\n2\n\b\u0003\u0010\r\u001a\u0004\u0018\u00010\nH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u0010"}, d2 = {"Lcom/audioscribe/app/data/network/WhisperApiService;", "", "transcribeAudio", "Lretrofit2/Response;", "Lcom/audioscribe/app/data/model/TranscriptionResponse;", "authorization", "", "file", "Lokhttp3/MultipartBody$Part;", "model", "Lokhttp3/RequestBody;", "language", "responseFormat", "temperature", "(Ljava/lang/String;Lokhttp3/MultipartBody$Part;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public abstract interface WhisperApiService {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BASE_URL = "https://api.openai.com/v1/";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_MODEL = "whisper-1";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_RESPONSE_FORMAT = "json";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEFAULT_LANGUAGE = "en";
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.data.network.WhisperApiService.Companion Companion = null;
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "audio/transcriptions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object transcribeAudio(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String authorization, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @retrofit2.http.Part(value = "model")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody model, @retrofit2.http.Part(value = "language")
    @org.jetbrains.annotations.Nullable()
    okhttp3.RequestBody language, @retrofit2.http.Part(value = "response_format")
    @org.jetbrains.annotations.Nullable()
    okhttp3.RequestBody responseFormat, @retrofit2.http.Part(value = "temperature")
    @org.jetbrains.annotations.Nullable()
    okhttp3.RequestBody temperature, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.audioscribe.app.data.model.TranscriptionResponse>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/audioscribe/app/data/network/WhisperApiService$Companion;", "", "()V", "BASE_URL", "", "DEFAULT_LANGUAGE", "DEFAULT_MODEL", "DEFAULT_RESPONSE_FORMAT", "createAuthHeader", "apiKey", "createRequestBody", "Lokhttp3/RequestBody;", "value", "app_debug"})
    public static final class Companion {
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BASE_URL = "https://api.openai.com/v1/";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String DEFAULT_MODEL = "whisper-1";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String DEFAULT_RESPONSE_FORMAT = "json";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String DEFAULT_LANGUAGE = "en";
        
        private Companion() {
            super();
        }
        
        /**
         * Create authorization header value
         */
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createAuthHeader(@org.jetbrains.annotations.NotNull()
        java.lang.String apiKey) {
            return null;
        }
        
        /**
         * Create request body for form data
         */
        @org.jetbrains.annotations.NotNull()
        public final okhttp3.RequestBody createRequestBody(@org.jetbrains.annotations.NotNull()
        java.lang.String value) {
            return null;
        }
    }
    
    /**
     * Retrofit service interface for OpenAI Whisper API
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}