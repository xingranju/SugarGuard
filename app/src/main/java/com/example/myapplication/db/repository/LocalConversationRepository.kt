package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.ConversationDao
import com.example.myapplication.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class LocalConversationRepository(private val conversationDao: ConversationDao) {

    fun getAllConversations(userId: Long): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations(userId)

    fun getSessionConversations(userId: Long, sessionId: String): Flow<List<ConversationEntity>> =
        conversationDao.getSessionConversations(userId, sessionId)

    suspend fun addMessage(userId: Long, role: String, content: String, sessionId: String? = null): Long {
        val conversation = ConversationEntity(
            userId = userId,
            sessionId = sessionId ?: UUID.randomUUID().toString(),
            role = role,
            content = content,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        return conversationDao.insert(conversation)
    }

    suspend fun updateFeedback(id: Long, feedback: String) {
        conversationDao.updateFeedback(id, feedback)
    }

    suspend fun deleteSession(userId: Long, sessionId: String) {
        conversationDao.deleteSession(userId, sessionId)
    }
}
