package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import retrofit2.Call
import retrofit2.http.*

data class CreateFamilyRequest(val name: String)
data class JoinFamilyRequest(val inviteCode: String)
data class FamilyGroupInfo(val id: Long = 0, val name: String = "", val inviteCode: String = "", val memberCount: Int = 0, val createdAt: String? = null)
data class FamilyMemberInfo(val id: Long = 0, val userId: Long = 0, val username: String = "", val avatarUrl: String? = null, val role: String = "member", val nickname: String? = null, val todaySugar: Float? = null, val sugarLimit: Float? = null, val streak: Int? = null, val lastCheckIn: String? = null)
data class HealthAlertInfo(val id: Long = 0, val userId: Long = 0, val username: String? = null, val alertType: String = "", val message: String = "", val severity: String = "info", val isRead: Boolean = false, val createdAt: String? = null)

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
}
