package com.audioscribe.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.TranscriptChunk
import com.audioscribe.app.data.database.dao.TranscriptionSessionDao
import com.audioscribe.app.data.database.dao.TranscriptChunkDao
import com.audioscribe.app.data.database.converter.Converters
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Room database for Audioscribe app
 * Stores transcription sessions and transcript chunks
 */
@Database(
    entities = [
        TranscriptionSession::class,
        TranscriptChunk::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AudioscribeDatabase : RoomDatabase() {
    /**
     * Get the TranscriptionSession DAO
     */
    abstract fun sessionDao(): TranscriptionSessionDao

    /**
     * Get the TranscriptChunk DAO
     */
    abstract fun chunkDao(): TranscriptChunkDao

    companion object {
        private const val DATABASE_NAME = "audioscribe_encrypted.db"

        @Volatile
        private var INSTANCE: AudioscribeDatabase? = null

        /**
         * Get the singleton database instance
         */
        fun getInstance(context: Context): AudioscribeDatabase {
            return INSTANCE ?: synchronized(this) {
                // Ensure SQLCipher native libs are loaded
                SQLiteDatabase.loadLibs(context)
                val passphrase = DatabaseKeyStore.getOrCreatePassphrase(context)
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioscribeDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * Create an in-memory database for testing
         */
        fun createInMemoryDatabase(context: Context): AudioscribeDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AudioscribeDatabase::class.java
            ).build()
        }

        /**
         * Close the database instance (useful for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
