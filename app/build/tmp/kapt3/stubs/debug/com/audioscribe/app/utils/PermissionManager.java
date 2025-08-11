package com.audioscribe.app.utils;

/**
 * Manages permissions required for audio capture functionality
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0007J\u0019\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\bJb\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00042\u000e\u0010\u0015\u001a\n\u0012\u0006\b\u0001\u0012\u00020\b0\u00072\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00130\u00192'\u0010\u001a\u001a#\u0012\u0019\u0012\u0017\u0012\u0004\u0012\u00020\b0\u001c\u00a2\u0006\f\b\u001d\u0012\b\b\u001e\u0012\u0004\b\b(\u001f\u0012\u0004\u0012\u00020\u00130\u001b\u00a2\u0006\u0002\u0010 J\u000e\u0010!\u001a\u00020\"2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010#\u001a\u00020\u00132\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010$\u001a\u00020\u00132\u0006\u0010%\u001a\u00020&J\u000e\u0010'\u001a\u00020\"2\u0006\u0010%\u001a\u00020&R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\t\u00a8\u0006("}, d2 = {"Lcom/audioscribe/app/utils/PermissionManager;", "", "()V", "REQUEST_CODE_MEDIA_PROJECTION", "", "REQUEST_CODE_PERMISSIONS", "REQUIRED_PERMISSIONS", "", "", "[Ljava/lang/String;", "getMediaProjectionIntent", "Landroid/content/Intent;", "context", "Landroid/content/Context;", "getMissingPermissions", "(Landroid/content/Context;)[Ljava/lang/String;", "getPermissionDisplayName", "permission", "handlePermissionResult", "", "requestCode", "permissions", "grantResults", "", "onAllGranted", "Lkotlin/Function0;", "onDenied", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "deniedPermissions", "(I[Ljava/lang/String;[ILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V", "hasAllPermissions", "", "openAppSettings", "requestPermissions", "activity", "Landroid/app/Activity;", "shouldShowRationale", "app_debug"})
public final class PermissionManager {
    public static final int REQUEST_CODE_MEDIA_PROJECTION = 1000;
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String[] REQUIRED_PERMISSIONS = {"android.permission.RECORD_AUDIO", "android.permission.FOREGROUND_SERVICE", "android.permission.POST_NOTIFICATIONS"};
    @org.jetbrains.annotations.NotNull()
    public static final com.audioscribe.app.utils.PermissionManager INSTANCE = null;
    
    private PermissionManager() {
        super();
    }
    
    /**
     * Check if all required permissions are granted
     */
    public final boolean hasAllPermissions(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Get list of permissions that are not granted
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String[] getMissingPermissions(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Request missing permissions
     */
    public final void requestPermissions(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
    
    /**
     * Check if we should show rationale for any permission
     */
    public final boolean shouldShowRationale(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
        return false;
    }
    
    /**
     * Get MediaProjection permission intent
     */
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent getMediaProjectionIntent(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Open app settings for manual permission granting
     */
    public final void openAppSettings(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Handle permission request results
     */
    public final void handlePermissionResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAllGranted, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.util.List<java.lang.String>, kotlin.Unit> onDenied) {
    }
    
    /**
     * Get human-readable permission names for UI display
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPermissionDisplayName(@org.jetbrains.annotations.NotNull()
    java.lang.String permission) {
        return null;
    }
}