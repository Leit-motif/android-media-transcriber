package com.audioscribe.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Application class for Audioscribe
 * Initializes WorkManager and other global components
 */
class AudioscribeApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom configuration if needed
        // WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
