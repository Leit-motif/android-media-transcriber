package com.audioscribe.app.data.database.converter

import androidx.room.TypeConverter
import com.audioscribe.app.data.database.entity.SessionStatus
import com.audioscribe.app.data.database.entity.ChunkStatus
import java.util.Date

/**
 * Type converters for Room database to handle custom data types
 */
class Converters {
    
    /**
     * Convert Date to Long timestamp for storage
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convert Long timestamp to Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    /**
     * Convert SessionStatus enum to String for storage
     */
    @TypeConverter
    fun fromSessionStatus(status: SessionStatus): String {
        return status.name
    }
    
    /**
     * Convert String to SessionStatus enum
     */
    @TypeConverter
    fun toSessionStatus(status: String): SessionStatus {
        return SessionStatus.valueOf(status)
    }
    
    /**
     * Convert ChunkStatus enum to String for storage
     */
    @TypeConverter
    fun fromChunkStatus(status: ChunkStatus): String {
        return status.name
    }
    
    /**
     * Convert String to ChunkStatus enum
     */
    @TypeConverter
    fun toChunkStatus(status: String): ChunkStatus {
        return ChunkStatus.valueOf(status)
    }
}
