package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 对话历史数据模型
 */
data class ConversationHistory(
    @SerializedName("conversation_id")
    val conversationId: Int,
    
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("response")
    val response: String,
    
    @SerializedName("intent")
    val intent: String?,
    
    @SerializedName("context_data")
    val contextData: String?,
    
    @SerializedName("feedback")
    val feedback: Int?,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * 更新反馈请求
 */
data class UpdateFeedbackRequest(
    @SerializedName("feedback")
    val feedback: Int
)






