package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

data class NotificationSettingsDto(
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("sugar_alert") val sugarAlert: Boolean? = null,
    @SerializedName("record_reminder") val recordReminder: Boolean? = null,
    @SerializedName("meal_reminder") val mealReminder: Boolean? = null,
    @SerializedName("water_reminder") val waterReminder: Boolean? = null,
    @SerializedName("weekly_report") val weeklyReport: Boolean? = null,
    @SerializedName("quiet_start") val quietStart: String? = null,
    @SerializedName("quiet_end") val quietEnd: String? = null
)

interface NotificationSettingsApiService {

    @GET("api/notification-settings")
    fun getSettings(@Query("userId") userId: Long): Call<ApiResponse<NotificationSettingsDto>>

    @PUT("api/notification-settings")
    fun updateSettings(
        @Query("userId") userId: Long,
        @Body dto: NotificationSettingsDto
    ): Call<ApiResponse<NotificationSettingsDto>>
}
