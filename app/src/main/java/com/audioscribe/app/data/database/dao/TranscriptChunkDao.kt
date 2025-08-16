package com.audioscribe.app.data.database.dao

import androidx.room.*
import com.audioscribe.app.data.database.entity.TranscriptChunk
import com.audioscribe.app.data.database.entity.ChunkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TranscriptChunk entities
 */
@Dao
interface TranscriptChunkDao {
    
    /**
     * Insert a new transcript chunk
     * @return The ID of the inserted chunk
     */
    @Insert
    suspend fun insertChunk(chunk: TranscriptChunk): Long
    
    /**
     * Insert multiple chunks
     */
    @Insert
    suspend fun insertChunks(chunks: List<TranscriptChunk>): List<Long>
    
    /**
     * Update an existing transcript chunk
     */
    @Update
    suspend fun updateChunk(chunk: TranscriptChunk)
    
    /**
     * Delete a transcript chunk
     */
    @Delete
    suspend fun deleteChunk(chunk: TranscriptChunk)
    
    /**
     * Delete chunk by ID
     */
    @Query("DELETE FROM transcript_chunks WHERE id = :chunkId")
    suspend fun deleteChunkById(chunkId: Long)
    
    /**
     * Get a chunk by ID
     */
    @Query("SELECT * FROM transcript_chunks WHERE id = :chunkId")
    suspend fun getChunkById(chunkId: Long): TranscriptChunk?
    
    /**
     * Get all chunks for a session ordered by chunk index
     */
    @Query("SELECT * FROM transcript_chunks WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    fun getChunksForSession(sessionId: Long): Flow<List<TranscriptChunk>>
    
    /**
     * Get chunks for a session synchronously (for immediate access)
     */
    @Query("SELECT * FROM transcript_chunks WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    suspend fun getChunksForSessionSync(sessionId: Long): List<TranscriptChunk>
    
    /**
     * Get chunks by status
     */
    @Query("SELECT * FROM transcript_chunks WHERE status = :status ORDER BY createdAt ASC")
    fun getChunksByStatus(status: ChunkStatus): Flow<List<TranscriptChunk>>
    
    /**
     * Get pending chunks for processing
     */
    @Query("SELECT * FROM transcript_chunks WHERE status IN ('PENDING', 'RETRYING') ORDER BY createdAt ASC")
    suspend fun getPendingChunks(): List<TranscriptChunk>
    
    /**
     * Update chunk status
     */
    @Query("UPDATE transcript_chunks SET status = :status, updatedAt = :updatedAt WHERE id = :chunkId")
    suspend fun updateChunkStatus(chunkId: Long, status: ChunkStatus, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update chunk transcription result
     */
    @Query("""
        UPDATE transcript_chunks 
        SET text = :text, 
            confidence = :confidence, 
            detectedLanguage = :detectedLanguage,
            status = :status,
            transcriptionCompletedAt = :completedAt,
            updatedAt = :updatedAt 
        WHERE id = :chunkId
    """)
    suspend fun updateChunkTranscriptionResult(
        chunkId: Long,
        text: String,
        confidence: Float?,
        detectedLanguage: String?,
        status: ChunkStatus,
        completedAt: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Update chunk error information
     */
    @Query("""
        UPDATE transcript_chunks 
        SET status = :status,
            errorMessage = :errorMessage,
            retryCount = retryCount + 1,
            updatedAt = :updatedAt 
        WHERE id = :chunkId
    """)
    suspend fun updateChunkError(
        chunkId: Long,
        status: ChunkStatus,
        errorMessage: String,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Mark chunk as processing
     */
    @Query("""
        UPDATE transcript_chunks 
        SET status = 'PROCESSING',
            transcriptionStartedAt = :startedAt,
            updatedAt = :updatedAt 
        WHERE id = :chunkId
    """)
    suspend fun markChunkAsProcessing(
        chunkId: Long,
        startedAt: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * Get combined text for all completed chunks in a session
     */
    @Query("""
        SELECT text FROM transcript_chunks 
        WHERE sessionId = :sessionId AND status = 'COMPLETED' 
        ORDER BY chunkIndex ASC
    """)
    suspend fun getCombinedTextForSession(sessionId: Long): List<String>
    
    /**
     * Get chunk count by status for a session
     */
    @Query("SELECT COUNT(*) FROM transcript_chunks WHERE sessionId = :sessionId AND status = :status")
    suspend fun getChunkCountByStatus(sessionId: Long, status: ChunkStatus): Int
    
    /**
     * Get total chunk count for a session
     */
    @Query("SELECT COUNT(*) FROM transcript_chunks WHERE sessionId = :sessionId")
    suspend fun getTotalChunkCount(sessionId: Long): Int
    
    /**
     * Delete chunks older than specified timestamp
     */
    @Query("DELETE FROM transcript_chunks WHERE createdAt < :olderThan")
    suspend fun deleteOldChunks(olderThan: Long): Int
    
    /**
     * Search chunks by text content
     */
    @Query("SELECT * FROM transcript_chunks WHERE text LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchChunksByText(searchQuery: String): Flow<List<TranscriptChunk>>
}
