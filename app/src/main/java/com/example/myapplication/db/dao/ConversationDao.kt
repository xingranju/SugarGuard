package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllConversations(userId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE user_id = :userId ORDER BY created_at ASC")
    suspend fun getAllConversationsSync(userId: Long): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE user_id = :userId AND session_id = :sessionId ORDER BY created_at ASC")
    fun getSessionConversations(userId: Long, sessionId: String): Flow<List<ConversationEntity>>

    @Query("SELECT DISTINCT session_id FROM conversations WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getSessionIds(userId: Long): List<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity): Long

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET feedback = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: Long, feedback: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM conversations WHERE user_id = :userId AND session_id = :sessionId")
    suspend fun deleteSession(userId: Long, sessionId: String)
}
