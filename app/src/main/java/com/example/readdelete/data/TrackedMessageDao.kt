package com.example.readdelete.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedMessageDao {
    @Insert
    suspend fun insert(message: TrackedMessage): Long

    @Query("SELECT * FROM tracked_messages ORDER BY sentAtMillis DESC")
    fun observeAll(): Flow<List<TrackedMessage>>

    @Query("UPDATE tracked_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM tracked_messages WHERE id = :id")
    suspend fun delete(id: Long)
}
