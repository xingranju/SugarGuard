package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "session_id")
    val sessionId: String? = null,
    val role: String,
    val content: String,
    @ColumnInfo(name = "message_type")
    val messageType: String = "text",
    val feedback: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
