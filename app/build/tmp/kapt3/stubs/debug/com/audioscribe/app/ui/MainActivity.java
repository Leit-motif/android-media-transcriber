package com.audioscribe.app.ui;

/**
 * Main Activity for Audioscribe
 * Handles permissions and controls audio capture service
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\b\b\u0007\u0018\u0000 82\u00020\u0001:\u00018B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010 \u001a\u00020!H\u0002J\u0014\u0010\"\u001a\u00020\u00042\n\u0010#\u001a\u0006\u0012\u0002\b\u00030$H\u0002J\u0012\u0010%\u001a\u00020!2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0014J\u0010\u0010(\u001a\u00020!2\u0006\u0010)\u001a\u00020\u0014H\u0014J\b\u0010*\u001a\u00020!H\u0014J\b\u0010+\u001a\u00020!H\u0014J\b\u0010,\u001a\u00020!H\u0002J\b\u0010-\u001a\u00020!H\u0002J\b\u0010.\u001a\u00020!H\u0002J\u0018\u0010/\u001a\u00020!2\u0006\u00100\u001a\u0002012\u0006\u00102\u001a\u00020\u0014H\u0002J\b\u00103\u001a\u00020!H\u0002J\b\u00104\u001a\u00020!H\u0002J\b\u00105\u001a\u00020!H\u0002J\b\u00106\u001a\u00020!H\u0002J\b\u00107\u001a\u00020!H\u0002R+\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\n\u0010\u000b\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR+\u0010\f\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000e\u0010\u000b\u001a\u0004\b\f\u0010\u0007\"\u0004\b\r\u0010\tR+\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0011\u0010\u000b\u001a\u0004\b\u000f\u0010\u0007\"\u0004\b\u0010\u0010\tR\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00160\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010\u001a\u001a\u00020\u00172\u0006\u0010\u0003\u001a\u00020\u00178B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001f\u0010\u000b\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001e\u00a8\u00069"}, d2 = {"Lcom/audioscribe/app/ui/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "<set-?>", "", "hasPermissions", "getHasPermissions", "()Z", "setHasPermissions", "(Z)V", "hasPermissions$delegate", "Landroidx/compose/runtime/MutableState;", "isProcessing", "setProcessing", "isProcessing$delegate", "isRecording", "setRecording", "isRecording$delegate", "mediaProjectionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "permissionsLauncher", "", "", "resultsReceiver", "Landroid/content/BroadcastReceiver;", "transcriptionResult", "getTranscriptionResult", "()Ljava/lang/String;", "setTranscriptionResult", "(Ljava/lang/String;)V", "transcriptionResult$delegate", "clearResults", "", "isServiceRunning", "serviceClass", "Ljava/lang/Class;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onNewIntent", "intent", "onPause", "onResume", "registerResultsReceiver", "requestPermissions", "showPermissionRationale", "startAudioCaptureService", "resultCode", "", "data", "startMicrophoneRecording", "startRecording", "stopRecording", "syncRecordingState", "unregisterResultsReceiver", "Companion", "app_debug"})
@androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.Q)
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "MainActivity";
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isRecording$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState hasPermissions$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState transcriptionResult$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState isProcessing$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private android.content.BroadcastReceiver resultsReceiver;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> mediaProjectionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> permissionsLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.ui.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super(0);
    }
    
    private final boolean isRecording() {
        return false;
    }
    
    private final void setRecording(boolean p0) {
    }
    
    private final boolean getHasPermissions() {
        return false;
    }
    
    private final void setHasPermissions(boolean p0) {
    }
    
    private final java.lang.String getTranscriptionResult() {
        return null;
    }
    
    private final void setTranscriptionResult(java.lang.String p0) {
    }
    
    private final boolean isProcessing() {
        return false;
    }
    
    private final void setProcessing(boolean p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    private final void syncRecordingState() {
    }
    
    @kotlin.Suppress(names = {"DEPRECATION"})
    private final boolean isServiceRunning(java.lang.Class<?> serviceClass) {
        return false;
    }
    
    private final void requestPermissions() {
    }
    
    private final void startRecording() {
    }
    
    private final void stopRecording() {
    }
    
    private final void clearResults() {
    }
    
    private final void registerResultsReceiver() {
    }
    
    private final void unregisterResultsReceiver() {
    }
    
    private final void startAudioCaptureService(int resultCode, android.content.Intent data) {
    }
    
    private final void startMicrophoneRecording() {
    }
    
    private final void showPermissionRationale() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/audioscribe/app/ui/MainActivity$Companion;", "", "()V", "TAG", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}