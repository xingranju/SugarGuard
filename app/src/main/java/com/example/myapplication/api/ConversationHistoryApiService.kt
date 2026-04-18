package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.ConversationHistory
import com.example.myapplication.model.UpdateFeedbackRequest
import retrofit2.Call
import retrofit2.http.*

/**
 * 对话历史API服务接口
 */
interface ConversationHistoryApiService {
    
    /**
     * 获取用户的对话历史
     */
    @GET("api/conversations")
    fun getUserConversations(
        @Query("limit") limit: Int = 20
    ): Call<ApiResponse<List<ConversationHistory>>>
    
    /**
     * 获取所有对话历史
     */
    @GET("api/conversations/all")
    fun getAllConversations(): Call<ApiResponse<List<ConversationHistory>>>
    
    /**
     * 更新对话反馈
     */
    @PUT("api/conversations/{conversationId}/feedback")
    fun updateFeedback(
        @Path("conversationId") conversationId: Int,
        @Body request: UpdateFeedbackRequest
    ): Call<ApiResponse<Void>>
    
    /**
     * 删除对话记录
     */
    @DELETE("api/conversations/{conversationId}")
    fun deleteConversation(
        @Path("conversationId") conversationId: Int
    ): Call<ApiResponse<Void>>
}






