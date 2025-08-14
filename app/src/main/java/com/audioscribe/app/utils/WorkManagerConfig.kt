package com.audioscribe.app.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.BackoffPolicy
import java.util.concurrent.TimeUnit

/**
 * Configuration utility for WorkManager constraints and policies.
 * Provides centralized configuration for transcription work requests.
 */
object WorkManagerConfig {
    
    /**
     * Default constraints for transcription work
     */
    fun getTranscriptionConstraints(context: Context): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(getRequiredNetworkType(context))
            .setRequiresBatteryNotLow(shouldRequireBatteryNotLow(context))
            .setRequiresDeviceIdle(shouldRequireDeviceIdle(context))
            .setRequiresCharging(shouldRequireCharging(context))
            .build()
    }
    
    /**
     * Get the required network type based on user preferences
     * For now, we'll use CONNECTED (any network) but this could be configurable
     */
    private fun getRequiredNetworkType(context: Context): NetworkType {
        // TODO: Make this configurable in settings
        // For transcription, we need internet but don't necessarily need unmetered
        // since audio files are relatively small (30-second chunks)
        return NetworkType.CONNECTED
    }
    
    /**
     * Whether to require battery not low
     * Transcription is CPU intensive, so we should avoid draining low battery
     */
    private fun shouldRequireBatteryNotLow(context: Context): Boolean {
        // TODO: Make this configurable in settings
        return true
    }
    
    /**
     * Whether to require device idle
     * For real-time transcription, we don't want to wait for idle
     */
    private fun shouldRequireDeviceIdle(context: Context): Boolean {
        // TODO: Make this configurable in settings
        // For real-time transcription, we should NOT require device idle
        return false
    }
    
    /**
     * Whether to require charging
     * For real-time transcription, we don't want to wait for charging
     */
    private fun shouldRequireCharging(context: Context): Boolean {
        // TODO: Make this configurable in settings
        // For real-time transcription, we should NOT require charging
        return false
    }
    
    /**
     * Get backoff policy for transcription work
     */
    fun getBackoffPolicy(): BackoffPolicy {
        return BackoffPolicy.EXPONENTIAL
    }
    
    /**
     * Get initial backoff delay in seconds
     */
    fun getInitialBackoffDelaySeconds(): Long {
        return 15L // Start with 15 seconds, then exponential backoff
    }
    
    /**
     * Get maximum backoff delay in minutes
     */
    fun getMaxBackoffDelayMinutes(): Long {
        return 5L // Maximum 5 minutes between retries
    }
    
    /**
     * Get the time unit for backoff delays
     */
    fun getBackoffTimeUnit(): TimeUnit {
        return TimeUnit.SECONDS
    }
    
    /**
     * Get maximum number of retry attempts
     * WorkManager will automatically retry based on backoff policy
     */
    fun getMaxRetryAttempts(): Int {
        return 3 // Retry up to 3 times before giving up
    }
    
    /**
     * Enhanced constraints for background transcription (when app is backgrounded)
     * These are more restrictive to preserve battery and data
     */
    fun getBackgroundTranscriptionConstraints(context: Context): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only for background processing
            .setRequiresBatteryNotLow(true) // Don't drain low battery
            .setRequiresCharging(false) // Don't require charging for background work
            .setRequiresDeviceIdle(false) // Don't require idle for background work
            .build()
    }
    
    /**
     * Minimal constraints for urgent transcription (real-time processing)
     * These are less restrictive to ensure timely processing
     */
    fun getUrgentTranscriptionConstraints(context: Context): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Any network connection
            .setRequiresBatteryNotLow(false) // Allow even on low battery for urgent work
            .setRequiresCharging(false) // Don't require charging
            .setRequiresDeviceIdle(false) // Don't require idle
            .build()
    }
}
