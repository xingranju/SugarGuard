package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class NotificationDto(
    val id: Long? = null,
    @SerializedName("user_id") val userId: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val type: String? = null,
    @SerializedName("is_read") val isRead: Boolean? = false,
    @SerializedName("target_page") val targetPage: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("read_at") val readAt: String? = null
)

interface NotificationApiService {

    @GET("api/notifications")
    fun getNotifications(@Query("userId") userId: Long): Call<ApiResponse<List<NotificationDto>>>

    @GET("api/notifications/unread-count")
    fun getUnreadCount(@Query("userId") userId: Long): Call<ApiResponse<Long>>

    @PUT("api/notifications/{id}/read")
    fun markAsRead(@Path("id") id: Long, @Query("userId") userId: Long): Call<ApiResponse<NotificationDto>>

    @PUT("api/notifications/read-all")
    fun markAllAsRead(@Query("userId") userId: Long): Call<ApiResponse<Void>>
}
