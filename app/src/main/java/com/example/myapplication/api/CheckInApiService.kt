package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import retrofit2.Call
import retrofit2.http.*

data class CheckInRequest(val userId: Long, val sugarIntake: Float, val notes: String = "")
data class CheckInResponse(val id: Long = 0, val streak: Int = 0, val withinLimit: Boolean = false, val newBadges: List<BadgeInfo> = emptyList())
data class CheckInRecord(val id: Long = 0, val checkInDate: String = "", val sugarIntake: Float = 0f, val withinLimit: Boolean = false, val streak: Int = 0, val notes: String? = null)
data class BadgeInfo(val id: Long = 0, val name: String = "", val description: String = "", val iconName: String = "", val category: String = "", val conditionDesc: String? = null, val threshold: Int = 0, val earned: Boolean = false, val earnedAt: String? = null, val progress: Int = 0)
data class RankingItem(val rank: Int = 0, val userId: Long = 0, val username: String = "", val avatarUrl: String? = null, val streak: Int = 0, val totalCheckIns: Int = 0, val badgeCount: Int = 0)

interface CheckInApiService {
    @POST("api/checkin")
    fun checkIn(@Body request: CheckInRequest): Call<ApiResponse<CheckInResponse>>

    @GET("api/checkin/history")
    fun getHistory(@Query("userId") userId: Long): Call<ApiResponse<List<CheckInRecord>>>

    @GET("api/checkin/streak")
    fun getStreak(@Query("userId") userId: Long): Call<ApiResponse<Int>>

    @GET("api/checkin/badges")
    fun getBadges(@Query("userId") userId: Long): Call<ApiResponse<List<BadgeInfo>>>

    @GET("api/checkin/ranking")
    fun getRanking(@Query("top") top: Int = 20): Call<ApiResponse<List<RankingItem>>>
}
