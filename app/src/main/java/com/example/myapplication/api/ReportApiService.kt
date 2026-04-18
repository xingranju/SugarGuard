package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class HealthReportDto(
    val id: Long? = null,
    @SerializedName("user_id") val userId: Long? = null,
    @SerializedName("period_type") val periodType: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("avg_sugar") val avgSugar: Float? = null,
    @SerializedName("avg_calories") val avgCalories: Float? = null,
    @SerializedName("total_sugar") val totalSugar: Float? = null,
    @SerializedName("total_calories") val totalCalories: Float? = null,
    @SerializedName("over_days") val overDays: Int? = null,
    @SerializedName("total_days") val totalDays: Int? = null,
    @SerializedName("record_days") val recordDays: Int? = null,
    @SerializedName("sugar_limit") val sugarLimit: Float? = null,
    val score: Int? = null,
    val summary: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

interface ReportApiService {

    @GET("api/reports")
    fun getReports(
        @Query("userId") userId: Long,
        @Query("periodType") periodType: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Call<ApiResponse<List<HealthReportDto>>>

    @POST("api/reports/generate")
    fun generateReports(@Query("userId") userId: Long): Call<ApiResponse<String>>
}
