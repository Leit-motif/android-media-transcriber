package com.audioscribe.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a transcription session.
 * Each session represents a single recording session that may contain multiple transcript chunks.
 */
@Entity(tableName = "transcription_sessions")
data class TranscriptionSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Timestamp when the recording session started
     */
    val startTime: Date,
    
    /**
     * Timestamp when the recording session ended (null if still in progress)
     */
    val endTime: Date? = null,
    
    /**
     * Total duration of the recording session in milliseconds
     */
    val durationMs: Long = 0,
    
    /**
     * Current status of the session
     */
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    
    /**
     * Optional title/name for the session
     */
    val title: String? = null,
    
    /**
     * Optional notes or description for the session
     */
    val notes: String? = null,
    
    /**
     * Total number of audio chunks in this session
     */
    val chunkCount: Int = 0,
    
    /**
     * Number of chunks that have been successfully transcribed
     */
    val transcribedChunkCount: Int = 0,
    
    /**
     * Total size of all audio files in bytes (before deletion)
     */
    val totalAudioSizeBytes: Long = 0,
    
    /**
     * Timestamp when this record was created
     */
    val createdAt: Date = Date(),
    
    /**
     * Timestamp when this record was last updated
     */
    val updatedAt: Date = Date()
)

/**
 * Enum representing the status of a transcription session
 */
enum class SessionStatus {
    IN_PROGRESS,    // Recording is still ongoing
    COMPLETED,      // Recording finished, all chunks transcribed
    FAILED,         // Recording or transcription failed
    CANCELLED       // Recording was cancelled by user
}
