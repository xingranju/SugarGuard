package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class CreateFamilyRequest(val name: String)
data class JoinFamilyRequest(@SerializedName("invite_code") val inviteCode: String)
data class UpdateFamilyRequest(val name: String? = null, @SerializedName("avatar_url") val avatarUrl: String? = null, val description: String? = null)
data class FamilyGroupInfo(
    val id: Long = 0, val name: String = "",
    @SerializedName("invite_code") val inviteCode: String = "",
    @SerializedName("member_count") val memberCount: Int = 0,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val description: String? = null,
    @SerializedName("creator_id") val creatorId: Long = 0,
    @SerializedName("created_at") val createdAt: String? = null
)
data class FamilyMemberInfo(
    val id: Long = 0,
    @SerializedName("user_id") val userId: Long = 0,
    val username: String = "",
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val role: String = "member",
    val nickname: String? = null,
    @SerializedName("today_sugar") val todaySugar: Float? = null,
    @SerializedName("sugar_limit") val sugarLimit: Float? = null,
    val streak: Int? = null,
    @SerializedName("last_check_in") val lastCheckIn: String? = null
)
data class HealthAlertInfo(
    val id: Long = 0,
    @SerializedName("user_id") val userId: Long = 0,
    val username: String? = null,
    @SerializedName("alert_type") val alertType: String = "",
    val message: String = "",
    val severity: String = "info",
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

interface FamilyApiService {
    @POST("api/family/create")
    fun createFamily(@Query("userId") userId: Long, @Body request: CreateFamilyRequest): Call<ApiResponse<FamilyGroupInfo>>

    @POST("api/family/join")
    fun joinFamily(@Query("userId") userId: Long, @Body request: JoinFamilyRequest): Call<ApiResponse<FamilyGroupInfo>>

    @GET("api/family/my")
    fun getMyFamilies(@Query("userId") userId: Long): Call<ApiResponse<List<FamilyGroupInfo>>>

    @GET("api/family/{groupId}/members")
    fun getFamilyMembers(@Path("groupId") groupId: Long): Call<ApiResponse<List<FamilyMemberInfo>>>

    @DELETE("api/family/{groupId}/members/{targetUserId}")
    fun removeMember(@Path("groupId") groupId: Long, @Path("targetUserId") targetUserId: Long, @Query("operatorId") operatorId: Long): Call<ApiResponse<Any>>

    @GET("api/family/{groupId}/alerts")
    fun getAlerts(@Path("groupId") groupId: Long): Call<ApiResponse<List<HealthAlertInfo>>>

    @POST("api/family/{groupId}/check-alerts")
    fun checkAlerts(@Path("groupId") groupId: Long): Call<ApiResponse<List<HealthAlertInfo>>>

    @PUT("api/family/alerts/{alertId}/read")
    fun markAlertRead(@Path("alertId") alertId: Long): Call<ApiResponse<Any>>

    @PUT("api/family/{groupId}")
    fun updateFamily(@Path("groupId") groupId: Long, @Query("userId") userId: Long, @Body request: UpdateFamilyRequest): Call<ApiResponse<FamilyGroupInfo>>

    @DELETE("api/family/{groupId}")
    fun deleteFamily(@Path("groupId") groupId: Long, @Query("userId") userId: Long): Call<ApiResponse<Any>>

    @POST("api/family/{groupId}/leave")
    fun leaveFamily(@Path("groupId") groupId: Long, @Query("userId") userId: Long): Call<ApiResponse<Any>>
}
