package com.audioscribe.app.data.database.dao

import androidx.room.*
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.SessionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TranscriptionSession entities
 */
@Dao
interface TranscriptionSessionDao {
    
    /**
     * Insert a new transcription session
     * @return The ID of the inserted session
     */
    @Insert
    suspend fun insertSession(session: TranscriptionSession): Long
    
    /**
     * Update an existing transcription session
     */
    @Update
    suspend fun updateSession(session: TranscriptionSession)
    
    /**
     * Delete a transcription session (will cascade to delete chunks)
     */
    @Delete
    suspend fun deleteSession(session: TranscriptionSession)
    
    /**
     * Delete a session by ID
     */
    @Query("DELETE FROM transcription_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
    
    /**
     * Get a session by ID
     */
    @Query("SELECT * FROM transcription_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): TranscriptionSession?
    
    /**
     * Get all sessions ordered by creation date (newest first)
     */
    @Query("SELECT * FROM transcription_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<TranscriptionSession>>
    
    /**
     * Get sessions by status
     */
    @Query("SELECT * FROM transcription_sessions WHERE status = :status ORDER BY createdAt DESC")
    fun getSessionsByStatus(status: SessionStatus): Flow<List<TranscriptionSession>>
    
    /**
     * Get the most recent session
     */
    @Query("SELECT * FROM transcription_sessions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentSession(): TranscriptionSession?
    
    /**
     * Get sessions within a date range
     */
    @Query("SELECT * FROM transcription_sessions WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getSessionsInDateRange(startDate: Long, endDate: Long): Flow<List<TranscriptionSession>>
    
    /**
     * Update session status
     */
    @Query("UPDATE transcription_sessions SET status = :status, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update session end time and duration
     */
    @Query("UPDATE transcription_sessions SET endTime = :endTime, durationMs = :durationMs, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionEndTime(sessionId: Long, endTime: Long, durationMs: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update chunk counts for a session
     */
    @Query("UPDATE transcription_sessions SET chunkCount = :chunkCount, transcribedChunkCount = :transcribedChunkCount, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionChunkCounts(sessionId: Long, chunkCount: Int, transcribedChunkCount: Int, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Get session statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalSessions,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedSessions,
            SUM(durationMs) as totalDurationMs,
            SUM(totalAudioSizeBytes) as totalAudioBytes
        FROM transcription_sessions
    """)
    suspend fun getSessionStatistics(): SessionStatistics
    
    /**
     * Delete old sessions (older than specified timestamp)
     */
    @Query("DELETE FROM transcription_sessions WHERE createdAt < :olderThan")
    suspend fun deleteOldSessions(olderThan: Long): Int
}

/**
 * Data class for session statistics
 */
data class SessionStatistics(
    val totalSessions: Int,
    val completedSessions: Int,
    val totalDurationMs: Long,
    val totalAudioBytes: Long
)
