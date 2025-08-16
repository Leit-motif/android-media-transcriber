package com.audioscribe.app.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u001ah\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0007\u001a\b\u0010\u000f\u001a\u00020\u0001H\u0007\u001a\b\u0010\u0010\u001a\u00020\u0001H\u0007\u00a8\u0006\u0011"}, d2 = {"AudioscribeScreen", "", "hasPermissions", "", "isRecording", "isProcessing", "transcriptionResult", "", "sessionRepository", "Lcom/audioscribe/app/data/repository/SessionRepository;", "onRequestPermissions", "Lkotlin/Function0;", "onStartRecording", "onStopRecording", "onClearResults", "AudioscribeScreenPreview", "AudioscribeScreenWithResultsPreview", "app_debug"})
public final class MainActivityKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AudioscribeScreen(boolean hasPermissions, boolean isRecording, boolean isProcessing, @org.jetbrains.annotations.NotNull()
    java.lang.String transcriptionResult, @org.jetbrains.annotations.NotNull()
    com.audioscribe.app.data.repository.SessionRepository sessionRepository, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRequestPermissions, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartRecording, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStopRecording, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClearResults) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void AudioscribeScreenPreview() {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "With Results")
    @androidx.compose.runtime.Composable()
    public static final void AudioscribeScreenWithResultsPreview() {
    }
}