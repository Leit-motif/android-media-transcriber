package com.audioscribe.app.data.repository

import android.content.Context
import android.util.Log
import com.audioscribe.app.data.database.AudioscribeDatabase
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.TranscriptChunk
import com.audioscribe.app.data.database.entity.SessionStatus
import com.audioscribe.app.data.database.entity.ChunkStatus
import com.audioscribe.app.data.database.dao.SessionStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Repository for managing transcription sessions and chunks
 */
class SessionRepository(context: Context) {
    
    private val database = AudioscribeDatabase.getInstance(context)
    private val sessionDao = database.sessionDao()
    private val chunkDao = database.chunkDao()
    
    companion object {
        private const val TAG = "SessionRepository"
    }
    
    // Session operations
    
    /**
     * Create a new transcription session
     * @return The ID of the created session
     */
    suspend fun createSession(
        title: String? = null,
        notes: String? = null
    ): Long {
        val session = TranscriptionSession(
            startTime = Date(),
            title = title,
            notes = notes,
            status = SessionStatus.IN_PROGRESS
        )
        
        val sessionId = sessionDao.insertSession(session)
        Log.d(TAG, "Created new session with ID: $sessionId")
        return sessionId
    }
    
    /**
     * Start a new session (alias for createSession with a TranscriptionSession object)
     */
    suspend fun startNewSession(session: TranscriptionSession): Long {
        val sessionId = sessionDao.insertSession(session)
        Log.d(TAG, "Started new session with ID: $sessionId")
        return sessionId
    }
    
    /**
     * Update an existing session
     */
    suspend fun updateSession(session: TranscriptionSession) {
        sessionDao.updateSession(session)
        Log.d(TAG, "Updated session ${session.id}")
    }
    
    /**
     * Add a chunk to a session (compatible with TranscriptChunk entity)
     */
    suspend fun addChunkToSession(chunk: TranscriptChunk) {
        val chunkId = chunkDao.insertChunk(chunk)
        
        // Update session chunk count
        updateSessionChunkCounts(chunk.sessionId)
        
        Log.d(TAG, "Added chunk to session ${chunk.sessionId}")
    }
    
    /**
     * Complete a session
     */
    suspend fun completeSession(sessionId: Long, endTime: Date = Date()) {
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            val durationMs = endTime.time - session.startTime.time
            sessionDao.updateSessionEndTime(sessionId, endTime.time, durationMs)
            sessionDao.updateSessionStatus(sessionId, SessionStatus.COMPLETED)
            Log.d(TAG, "Completed session $sessionId with duration ${durationMs}ms")
        }
    }
    
    /**
     * Cancel a session
     */
    suspend fun cancelSession(sessionId: Long) {
        sessionDao.updateSessionStatus(sessionId, SessionStatus.CANCELLED)
        Log.d(TAG, "Cancelled session $sessionId")
    }
    
    /**
     * Mark a session as failed
     */
    suspend fun failSession(sessionId: Long) {
        sessionDao.updateSessionStatus(sessionId, SessionStatus.FAILED)
        Log.d(TAG, "Marked session $sessionId as failed")
    }
    
    /**
     * Get a session by ID
     */
    suspend fun getSession(sessionId: Long): TranscriptionSession? {
        return sessionDao.getSessionById(sessionId)
    }
    
    /**
     * Get all sessions
     */
    fun getAllSessions(): Flow<List<TranscriptionSession>> {
        return sessionDao.getAllSessions()
    }
    
    /**
     * Get the most recent session
     */
    suspend fun getMostRecentSession(): TranscriptionSession? {
        return sessionDao.getMostRecentSession()
    }
    
    /**
     * Delete a session and all its chunks
     */
    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSessionById(sessionId)
        Log.d(TAG, "Deleted session $sessionId")
    }
    
    // Chunk operations
    
    /**
     * Add a new transcript chunk to a session
     */
    suspend fun addChunk(
        sessionId: Long,
        chunkIndex: Int,
        originalFileName: String,
        durationMs: Long,
        audioFileSizeBytes: Long
    ): Long {
        val chunk = TranscriptChunk(
            sessionId = sessionId,
            chunkIndex = chunkIndex,
            text = "", // Will be filled when transcription completes
            durationMs = durationMs,
            audioFileSizeBytes = audioFileSizeBytes,
            originalFileName = originalFileName,
            status = ChunkStatus.PENDING
        )
        
        val chunkId = chunkDao.insertChunk(chunk)
        
        // Update session chunk count
        updateSessionChunkCounts(sessionId)
        
        Log.d(TAG, "Added chunk $chunkId to session $sessionId")
        return chunkId
    }
    
    /**
     * Mark a chunk as processing
     */
    suspend fun markChunkAsProcessing(chunkId: Long) {
        chunkDao.markChunkAsProcessing(chunkId, System.currentTimeMillis())
        Log.d(TAG, "Marked chunk $chunkId as processing")
    }
    
    /**
     * Complete a chunk transcription
     */
    suspend fun completeChunkTranscription(
        chunkId: Long,
        text: String,
        confidence: Float? = null,
        detectedLanguage: String? = null
    ) {
        chunkDao.updateChunkTranscriptionResult(
            chunkId = chunkId,
            text = text,
            confidence = confidence,
            detectedLanguage = detectedLanguage,
            status = ChunkStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )
        
        // Update session transcribed chunk count
        val chunk = chunkDao.getChunkById(chunkId)
        if (chunk != null) {
            updateSessionChunkCounts(chunk.sessionId)
        }
        
        Log.d(TAG, "Completed transcription for chunk $chunkId")
    }
    
    /**
     * Mark a chunk transcription as failed
     */
    suspend fun failChunkTranscription(chunkId: Long, errorMessage: String) {
        chunkDao.updateChunkError(
            chunkId = chunkId,
            status = ChunkStatus.FAILED,
            errorMessage = errorMessage
        )
        Log.d(TAG, "Failed transcription for chunk $chunkId: $errorMessage")
    }
    
    /**
     * Retry a failed chunk
     */
    suspend fun retryChunk(chunkId: Long) {
        chunkDao.updateChunkStatus(chunkId, ChunkStatus.RETRYING)
        Log.d(TAG, "Retrying chunk $chunkId")
    }
    
    /**
     * Get all chunks for a session
     */
    fun getChunksForSession(sessionId: Long): Flow<List<TranscriptChunk>> {
        return chunkDao.getChunksForSession(sessionId)
    }
    
    /**
     * Get combined transcription text for a session
     */
    suspend fun getCombinedTranscriptionText(sessionId: Long): String {
        val textChunks = chunkDao.getCombinedTextForSession(sessionId)
        return textChunks.joinToString(" ")
    }
    
    /**
     * Get pending chunks that need transcription
     */
    suspend fun getPendingChunks(): List<TranscriptChunk> {
        return chunkDao.getPendingChunks()
    }
    
    // Helper methods
    
    /**
     * Update chunk counts for a session
     */
    private suspend fun updateSessionChunkCounts(sessionId: Long) {
        val totalChunks = chunkDao.getTotalChunkCount(sessionId)
        val transcribedChunks = chunkDao.getChunkCountByStatus(sessionId, ChunkStatus.COMPLETED)
        
        sessionDao.updateSessionChunkCounts(sessionId, totalChunks, transcribedChunks)
    }
    
    /**
     * Get session statistics
     */
    suspend fun getSessionStatistics(): SessionStatistics {
        return sessionDao.getSessionStatistics()
    }
    
    /**
     * Search transcripts by text content
     */
    fun searchTranscripts(query: String): Flow<List<TranscriptChunk>> {
        return chunkDao.searchChunksByText(query)
    }
    
    /**
     * Clean up old sessions and chunks
     */
    suspend fun cleanupOldData(olderThanDays: Int = 30): Pair<Int, Int> {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        
        val deletedChunks = chunkDao.deleteOldChunks(cutoffTime)
        val deletedSessions = sessionDao.deleteOldSessions(cutoffTime)
        
        Log.d(TAG, "Cleaned up $deletedSessions old sessions and $deletedChunks old chunks")
        return Pair(deletedSessions, deletedChunks)
    }
}
