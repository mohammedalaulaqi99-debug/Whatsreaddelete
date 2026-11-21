package com.example.readdelete.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_messages")
data class TrackedMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipient: String,
    val messageText: String,
    val sentAtMillis: Long,
    val delaySeconds: Int,
    val isGroup: Boolean,
    val source: String, // "compose_send" or "existing_manual"
    val status: String = "REGISTERED",
    val readAtMillis: Long? = null,
    val deletedAtMillis: Long? = null
)
