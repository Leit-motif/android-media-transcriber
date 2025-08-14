package com.audioscribe.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.audioscribe.app.worker.FileCleanupWorker
import com.audioscribe.app.utils.WorkManagerConfig
import java.util.concurrent.TimeUnit

/**
 * Application class for Audioscribe
 * Initializes WorkManager and other global components
 */
class AudioscribeApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom configuration if needed
        // WorkManager.initialize(this, workManagerConfiguration)
        
        // Schedule periodic file cleanup
        scheduleFileCleanup()
    }
    
    /**
     * Schedule periodic file cleanup work
     */
    private fun scheduleFileCleanup() {
        val cleanupWork = PeriodicWorkRequestBuilder<FileCleanupWorker>(
            WorkManagerConfig.getCleanupIntervalHours(),
            TimeUnit.HOURS
        )
            .setConstraints(WorkManagerConfig.getFileCleanupConstraints(this))
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "file_cleanup",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            cleanupWork
        )
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
