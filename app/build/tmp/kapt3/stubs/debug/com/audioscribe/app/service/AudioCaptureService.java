package com.audioscribe.app.service;

/**
 * Foreground service for capturing system audio
 * Uses MediaProjection API with AudioPlaybackCapture to capture audio from other apps
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 ;2\u00020\u0001:\u0001;B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0013\u001a\u00020\u0006H\u0002J\b\u0010\u0014\u001a\u00020\u0015H\u0002J\b\u0010\u0016\u001a\u00020\u0015H\u0002J\u0012\u0010\u0017\u001a\u0004\u0018\u00010\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\b\u0010\u001b\u001a\u00020\u0015H\u0016J\b\u0010\u001c\u001a\u00020\u0015H\u0016J\"\u0010\u001d\u001a\u00020\u001e2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u001f\u001a\u00020\u001e2\u0006\u0010 \u001a\u00020\u001eH\u0016J\u000e\u0010!\u001a\u00020\u0015H\u0082@\u00a2\u0006\u0002\u0010\"J \u0010#\u001a\u00020\u00152\n\b\u0002\u0010$\u001a\u0004\u0018\u00010%2\n\b\u0002\u0010&\u001a\u0004\u0018\u00010%H\u0002J\u0010\u0010\'\u001a\u00020\u00152\u0006\u0010(\u001a\u00020%H\u0002J\u0010\u0010)\u001a\u00020\u00152\u0006\u0010*\u001a\u00020%H\u0002J\u0018\u0010+\u001a\u00020\u00152\u0006\u0010,\u001a\u00020\u001e2\u0006\u0010-\u001a\u00020\u001aH\u0002J\b\u0010.\u001a\u00020\u0015H\u0002J\u0010\u0010/\u001a\u00020\u00152\u0006\u00100\u001a\u00020\nH\u0002J\b\u00101\u001a\u00020\u0015H\u0002J\u001a\u00102\u001a\u00020\u00152\u0006\u00103\u001a\u00020\u00062\b\b\u0002\u00104\u001a\u00020\u0006H\u0002J\u0018\u00105\u001a\u00020\u00152\u0006\u00106\u001a\u00020\n2\u0006\u00107\u001a\u00020\u001eH\u0002J\u0018\u00108\u001a\u00020\u00152\u0006\u00109\u001a\u00020:2\u0006\u00107\u001a\u00020\u001eH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006<"}, d2 = {"Lcom/audioscribe/app/service/AudioCaptureService;", "Landroidx/lifecycle/LifecycleService;", "()V", "audioRecord", "Landroid/media/AudioRecord;", "isRecording", "", "mediaProjection", "Landroid/media/projection/MediaProjection;", "outputFile", "Ljava/io/File;", "recordingJob", "Lkotlinx/coroutines/Job;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "transcriptionRepository", "Lcom/audioscribe/app/data/repository/TranscriptionRepository;", "createNotification", "Landroid/app/Notification;", "isMicrophoneMode", "createNotificationChannel", "", "createOutputFile", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "recordAudio", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendTranscriptionBroadcast", "text", "", "error", "showErrorToast", "message", "showTranscriptionResult", "transcriptionText", "startAudioCapture", "resultCode", "resultData", "startMicrophoneRecording", "startTranscription", "audioFile", "stopAudioCapture", "updateNotificationForTranscription", "isProcessing", "hasResult", "updateWavHeader", "file", "dataSize", "writeWavHeader", "fos", "Ljava/io/FileOutputStream;", "Companion", "app_debug"})
@androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.Q)
public final class AudioCaptureService extends androidx.lifecycle.LifecycleService {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AudioCaptureService";
    private static final int NOTIFICATION_ID = 1;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "audio_capture_channel";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_NAME = "Audio Capture";
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_STEREO;
    private static final int AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHUNK_SECONDS = 30;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_TRANSCRIPTION_COMPLETE = "ACTION_TRANSCRIPTION_COMPLETE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_TRANSCRIPTION_TEXT = "EXTRA_TRANSCRIPTION_TEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_TRANSCRIPTION_ERROR = "EXTRA_TRANSCRIPTION_ERROR";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_START_CAPTURE = "ACTION_START_CAPTURE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP_CAPTURE = "ACTION_STOP_CAPTURE";
    @org.jetbrains.annotations.Nullable()
    private android.media.projection.MediaProjection mediaProjection;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioRecord audioRecord;
    private boolean isRecording = false;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job recordingJob;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.Nullable()
    private java.io.File outputFile;
    @org.jetbrains.annotations.NotNull()
    private final com.audioscribe.app.data.repository.TranscriptionRepository transcriptionRepository = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.service.AudioCaptureService.Companion Companion = null;
    
    public AudioCaptureService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void startAudioCapture(int resultCode, android.content.Intent resultData) {
    }
    
    private final void stopAudioCapture() {
    }
    
    private final java.lang.Object recordAudio(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void createOutputFile() {
    }
    
    private final void writeWavHeader(java.io.FileOutputStream fos, int dataSize) {
    }
    
    private final void updateWavHeader(java.io.File file, int dataSize) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
        return null;
    }
    
    private final void startMicrophoneRecording() {
    }
    
    private final void showErrorToast(java.lang.String message) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final android.app.Notification createNotification(boolean isRecording, boolean isMicrophoneMode) {
        return null;
    }
    
    /**
     * Start transcription process for the recorded audio file
     */
    private final void startTranscription(java.io.File audioFile) {
    }
    
    /**
     * Update notification to show transcription status
     */
    private final void updateNotificationForTranscription(boolean isProcessing, boolean hasResult) {
    }
    
    /**
     * Show transcription result to user
     */
    private final void showTranscriptionResult(java.lang.String transcriptionText) {
    }
    
    private final void sendTranscriptionBroadcast(java.lang.String text, java.lang.String error) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\f\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/audioscribe/app/service/AudioCaptureService$Companion;", "", "()V", "ACTION_START_CAPTURE", "", "ACTION_STOP_CAPTURE", "ACTION_TRANSCRIPTION_COMPLETE", "AUDIO_FORMAT", "", "CHANNEL_CONFIG", "CHANNEL_ID", "CHANNEL_NAME", "CHUNK_SECONDS", "EXTRA_RESULT_CODE", "EXTRA_RESULT_DATA", "EXTRA_TRANSCRIPTION_ERROR", "EXTRA_TRANSCRIPTION_TEXT", "NOTIFICATION_ID", "SAMPLE_RATE", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}