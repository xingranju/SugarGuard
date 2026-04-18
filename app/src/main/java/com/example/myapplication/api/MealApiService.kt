package com.example.myapplication.api

import com.example.myapplication.model.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/** 与后端 MealService.convertToMap 字段一致（camelCase） */
data class RecentMealDto(
    val mealId: Int? = null,
    val mealDate: String? = null,
    val mealTime: String? = null,
    val mealType: String? = null,
    val foodName: String? = null,
    val sugarContent: Double? = null,
    val calories: Double? = null,
    @SerializedName("imagePath") val imagePath: String? = null,
    val notes: String? = null
)

/**
 * 饮食日记API接口
 */
interface MealApiService {
    
    /**
     * 添加饮食记录
     */
    @POST("api/meals")
    fun addMeal(@Body request: AddMealRequest): Call<ApiResponse<MealRecord>>
    
    /**
     * 添加饮食记录(带图片)
     */
    @Multipart
    @POST("api/meals/with-image")
    fun addMealWithImage(
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part("food_name") foodName: okhttp3.RequestBody,
        @Part("sugar_content") sugarContent: okhttp3.RequestBody,
        @Part("calories") calories: okhttp3.RequestBody,
        @Part("protein") protein: okhttp3.RequestBody?,
        @Part("fat") fat: okhttp3.RequestBody?,
        @Part("carbohydrate") carbohydrate: okhttp3.RequestBody?,
        @Part("portion_size") portionSize: okhttp3.RequestBody?,
        @Part("notes") notes: okhttp3.RequestBody,
        @Part("meal_type") mealType: okhttp3.RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ApiResponse<Map<String, Any>>>
    
    /**
     * 获取最近 N 天的饮食记录（扁平列表，按时间倒序由服务端查询）
     */
    @GET("api/meals/recent")
    fun getRecentMeals(
        @Query("user_id") userId: Int,
        @Query("days") days: Int
    ): Call<ApiResponse<List<RecentMealDto>>>

    /**
     * 获取指定日期的饮食记录
     */
    @GET("api/meals/daily")
    fun getDailyMeals(
        @Query("user_id") userId: Int,
        @Query("date") date: String // yyyy-MM-dd
    ): Call<ApiResponse<Map<String, Any>>>
    
    /**
     * 获取日期范围内的饮食记录
     */
    @GET("api/meals/range")
    fun getMealsByDateRange(
        @Query("user_id") userId: Int,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Call<ApiResponse<List<MealRecord>>>
    
    /**
     * 删除饮食记录
     */
    @DELETE("api/meals/{meal_id}")
    fun deleteMeal(
        @Path("meal_id") mealId: Int,
        @Query("user_id") userId: Int
    ): Call<ApiResponse<String>>
    
    /**
     * 更新饮食记录
     */
    @PUT("api/meals/{meal_id}")
    fun updateMeal(
        @Path("meal_id") mealId: Int,
        @Body request: AddMealRequest
    ): Call<ApiResponse<MealRecord>>
}

