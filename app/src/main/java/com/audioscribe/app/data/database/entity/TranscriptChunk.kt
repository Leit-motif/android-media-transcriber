package com.audioscribe.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

/**
 * Entity representing a single transcript chunk within a session.
 * Each chunk corresponds to a 30-second audio segment that was transcribed.
 */
@Entity(
    tableName = "transcript_chunks",
    foreignKeys = [
        ForeignKey(
            entity = TranscriptionSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["chunkIndex"]),
        Index(value = ["createdAt"])
    ]
)
data class TranscriptChunk(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * ID of the parent transcription session
     */
    val sessionId: Long,
    
    /**
     * Index of this chunk within the session (0-based)
     */
    val chunkIndex: Int,
    
    /**
     * The transcribed text content
     */
    val text: String,
    
    /**
     * Confidence score from the transcription API (0.0 to 1.0)
     */
    val confidence: Float? = null,
    
    /**
     * Detected language code (e.g., "en", "es", "fr")
     */
    val detectedLanguage: String? = null,
    
    /**
     * Duration of this audio chunk in milliseconds
     */
    val durationMs: Long,
    
    /**
     * Size of the original audio file in bytes (before deletion)
     */
    val audioFileSizeBytes: Long,
    
    /**
     * Original filename of the audio chunk
     */
    val originalFileName: String,
    
    /**
     * Status of this chunk's transcription
     */
    val status: ChunkStatus = ChunkStatus.PENDING,
    
    /**
     * Error message if transcription failed
     */
    val errorMessage: String? = null,
    
    /**
     * Number of retry attempts for this chunk
     */
    val retryCount: Int = 0,
    
    /**
     * Timestamp when transcription was requested
     */
    val transcriptionStartedAt: Date? = null,
    
    /**
     * Timestamp when transcription was completed
     */
    val transcriptionCompletedAt: Date? = null,
    
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
 * Enum representing the status of a transcript chunk
 */
enum class ChunkStatus {
    PENDING,        // Waiting to be transcribed
    PROCESSING,     // Currently being transcribed
    COMPLETED,      // Successfully transcribed
    FAILED,         // Transcription failed
    RETRYING        // Being retried after failure
}
