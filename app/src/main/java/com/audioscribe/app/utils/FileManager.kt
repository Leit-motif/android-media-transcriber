package com.audioscribe.app.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing local audio files and cleanup operations.
 * Handles file lifecycle, cleanup policies, and storage optimization.
 */
object FileManager {
    
    private const val TAG = "FileManager"
    
    // File age thresholds for cleanup
    private const val MAX_FILE_AGE_HOURS = 24L // Delete files older than 24 hours
    private const val MAX_FAILED_FILE_AGE_HOURS = 1L // Delete failed files after 1 hour
    
    /**
     * Delete a WAV file after successful transcription
     */
    fun deleteTranscribedFile(audioFile: File): Boolean {
        return try {
            if (audioFile.exists()) {
                val deleted = audioFile.delete()
                if (deleted) {
                    Log.i(TAG, "Successfully deleted transcribed file: ${audioFile.name}")
                } else {
                    Log.w(TAG, "Failed to delete transcribed file: ${audioFile.name}")
                }
                deleted
            } else {
                Log.d(TAG, "File already deleted or doesn't exist: ${audioFile.name}")
                true // Consider it successful if file doesn't exist
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception deleting file: ${audioFile.name}", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${audioFile.name}", e)
            false
        }
    }
    
    /**
     * Clean up old and failed transcription files
     */
    fun cleanupOldFiles(context: Context): CleanupResult {
        val audioDir = getAudioDirectory(context)
        if (!audioDir.exists()) {
            Log.d(TAG, "Audio directory doesn't exist, nothing to clean")
            return CleanupResult(0, 0, 0)
        }
        
        val currentTime = System.currentTimeMillis()
        val maxAgeMillis = TimeUnit.HOURS.toMillis(MAX_FILE_AGE_HOURS)
        val maxFailedAgeMillis = TimeUnit.HOURS.toMillis(MAX_FAILED_FILE_AGE_HOURS)
        
        var deletedCount = 0
        var failedCount = 0
        var totalSize = 0L
        
        try {
            audioDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".wav")) {
                    val fileAge = currentTime - file.lastModified()
                    val fileSize = file.length()
                    
                    val shouldDelete = when {
                        // Delete very old files regardless
                        fileAge > maxAgeMillis -> {
                            Log.d(TAG, "Deleting old file: ${file.name} (age: ${fileAge / 1000 / 60} minutes)")
                            true
                        }
                        // Delete failed transcription files more aggressively
                        isFailedTranscriptionFile(file) && fileAge > maxFailedAgeMillis -> {
                            Log.d(TAG, "Deleting failed transcription file: ${file.name}")
                            true
                        }
                        else -> false
                    }
                    
                    if (shouldDelete) {
                        if (file.delete()) {
                            deletedCount++
                            totalSize += fileSize
                            Log.d(TAG, "Deleted file: ${file.name} (${fileSize} bytes)")
                        } else {
                            failedCount++
                            Log.w(TAG, "Failed to delete file: ${file.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
        
        val result = CleanupResult(deletedCount, failedCount, totalSize)
        Log.i(TAG, "Cleanup completed: $result")
        return result
    }
    
    /**
     * Get the directory where audio files are stored
     */
    fun getAudioDirectory(context: Context): File {
        return context.getExternalFilesDir(null) ?: context.filesDir
    }
    
    /**
     * Get current storage usage information
     */
    fun getStorageInfo(context: Context): StorageInfo {
        val audioDir = getAudioDirectory(context)
        var fileCount = 0
        var totalSize = 0L
        var oldestFile: Long? = null
        var newestFile: Long? = null
        
        try {
            audioDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".wav")) {
                    fileCount++
                    totalSize += file.length()
                    
                    val modified = file.lastModified()
                    oldestFile = oldestFile?.let { minOf(it, modified) } ?: modified
                    newestFile = newestFile?.let { maxOf(it, modified) } ?: modified
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting storage info", e)
        }
        
        return StorageInfo(
            fileCount = fileCount,
            totalSizeBytes = totalSize,
            oldestFileTimestamp = oldestFile,
            newestFileTimestamp = newestFile,
            directoryPath = audioDir.absolutePath
        )
    }
    
    /**
     * Check if a file appears to be from a failed transcription
     * This is a heuristic based on file age and naming patterns
     */
    private fun isFailedTranscriptionFile(file: File): Boolean {
        // Files that are small (< 1KB) might be corrupted
        if (file.length() < 1024) {
            return true
        }
        
        // Add more heuristics as needed
        // For now, we rely mainly on age-based cleanup
        return false
    }
    
    /**
     * Format file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * Result of a cleanup operation
 */
data class CleanupResult(
    val deletedCount: Int,
    val failedCount: Int,
    val totalSizeFreed: Long
) {
    override fun toString(): String {
        return "Deleted $deletedCount files (${FileManager.formatFileSize(totalSizeFreed)}), $failedCount failures"
    }
}

/**
 * Information about current storage usage
 */
data class StorageInfo(
    val fileCount: Int,
    val totalSizeBytes: Long,
    val oldestFileTimestamp: Long?,
    val newestFileTimestamp: Long?,
    val directoryPath: String
) {
    override fun toString(): String {
        return "Files: $fileCount, Size: ${FileManager.formatFileSize(totalSizeBytes)}, Path: $directoryPath"
    }
}
