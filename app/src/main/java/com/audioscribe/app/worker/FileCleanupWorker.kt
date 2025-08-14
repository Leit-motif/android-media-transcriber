package com.audioscribe.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.audioscribe.app.utils.FileManager

/**
 * Background worker for cleaning up old and failed transcription files.
 * Runs periodically to maintain optimal local storage usage.
 */
class FileCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FileCleanupWorker"
        
        // Output data keys
        const val KEY_DELETED_COUNT = "deleted_count"
        const val KEY_FAILED_COUNT = "failed_count"
        const val KEY_SIZE_FREED = "size_freed"
        const val KEY_STORAGE_INFO = "storage_info"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting file cleanup work")
            
            // Get storage info before cleanup
            val storageInfoBefore = FileManager.getStorageInfo(applicationContext)
            Log.i(TAG, "Storage before cleanup: $storageInfoBefore")
            
            // Perform cleanup
            val cleanupResult = FileManager.cleanupOldFiles(applicationContext)
            Log.i(TAG, "Cleanup result: $cleanupResult")
            
            // Get storage info after cleanup
            val storageInfoAfter = FileManager.getStorageInfo(applicationContext)
            Log.i(TAG, "Storage after cleanup: $storageInfoAfter")
            
            // Create output data
            val outputData = Data.Builder()
                .putInt(KEY_DELETED_COUNT, cleanupResult.deletedCount)
                .putInt(KEY_FAILED_COUNT, cleanupResult.failedCount)
                .putLong(KEY_SIZE_FREED, cleanupResult.totalSizeFreed)
                .putString(KEY_STORAGE_INFO, storageInfoAfter.toString())
                .build()
            
            Log.i(TAG, "File cleanup completed successfully")
            Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during file cleanup work", e)
            
            val errorData = Data.Builder()
                .putString("error", e.message ?: "Unknown cleanup error")
                .build()
            
            Result.failure(errorData)
        }
    }
}
