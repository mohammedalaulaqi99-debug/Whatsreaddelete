package com.example.readdelete.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackedMessage::class], version = 1, exportSchema = false)
abstract class ReadDeleteDatabase : RoomDatabase() {
    abstract fun trackedMessageDao(): TrackedMessageDao

    companion object {
        @Volatile private var INSTANCE: ReadDeleteDatabase? = null

        fun get(context: Context): ReadDeleteDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReadDeleteDatabase::class.java,
                    "readdelete.db"
                ).build().also { INSTANCE = it }
            }
    }
}
