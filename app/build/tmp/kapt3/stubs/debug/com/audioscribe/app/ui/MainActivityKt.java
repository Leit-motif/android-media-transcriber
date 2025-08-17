package com.audioscribe.app.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0003\u001a\u008a\u0001\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000b2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u0011H\u0007\u001a\b\u0010\u0013\u001a\u00020\u0001H\u0007\u001a\b\u0010\u0014\u001a\u00020\u0001H\u0007\u001a8\u0010\u0015\u001a\u00020\u00012\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00120\u00172\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u00112\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u000bH\u0003\u00a8\u0006\u001a"}, d2 = {"AudioscribeScreen", "", "hasPermissions", "", "isRecording", "isProcessing", "transcriptionResult", "", "sessionRepository", "Lcom/audioscribe/app/data/repository/SessionRepository;", "onRequestPermissions", "Lkotlin/Function0;", "onStartRecording", "onStopRecording", "onClearResults", "requestMicPermission", "resolveIssue", "Lkotlin/Function1;", "Lcom/audioscribe/app/domain/permissions/PreflightIssue;", "AudioscribeScreenPreview", "AudioscribeScreenWithResultsPreview", "PermissionSheet", "issues", "", "onIssueAction", "onClose", "app_debug"})
public final class MainActivityKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AudioscribeScreen(boolean hasPermissions, boolean isRecording, boolean isProcessing, @org.jetbrains.annotations.NotNull()
    java.lang.String transcriptionResult, @org.jetbrains.annotations.NotNull()
    com.audioscribe.app.data.repository.SessionRepository sessionRepository, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRequestPermissions, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartRecording, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStopRecording, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClearResults, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> requestMicPermission, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.audioscribe.app.domain.permissions.PreflightIssue, kotlin.Unit> resolveIssue) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void PermissionSheet(java.util.List<? extends com.audioscribe.app.domain.permissions.PreflightIssue> issues, kotlin.jvm.functions.Function1<? super com.audioscribe.app.domain.permissions.PreflightIssue, kotlin.Unit> onIssueAction, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
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