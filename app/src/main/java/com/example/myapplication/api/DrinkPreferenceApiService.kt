package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.Drink
import com.example.myapplication.model.DrinkPreferenceRequest
import com.example.myapplication.model.UserDrinkPreference
import retrofit2.Call
import retrofit2.http.*

/**
 * 饮品偏好API服务接口
 */
interface DrinkPreferenceApiService {
    
    /**
     * 获取用户所有饮品偏好
     */
    @GET("api/drink-preferences/preferences")
    fun getUserPreferences(): Call<ApiResponse<List<UserDrinkPreference>>>
    
    /**
     * 添加或更新饮品偏好
     */
    @POST("api/drink-preferences/preferences")
    fun addOrUpdatePreference(
        @Body request: DrinkPreferenceRequest
    ): Call<ApiResponse<UserDrinkPreference>>
    
    /**
     * 记录饮用
     */
    @POST("api/drink-preferences/preferences/{drinkId}/consume")
    fun recordConsumption(
        @Path("drinkId") drinkId: Int
    ): Call<ApiResponse<UserDrinkPreference>>
    
    /**
     * 删除饮品偏好
     */
    @DELETE("api/drink-preferences/preferences/{drinkId}")
    fun deletePreference(
        @Path("drinkId") drinkId: Int
    ): Call<ApiResponse<Void>>
    
    /**
     * 更新偏好评分
     */
    @PUT("api/drink-preferences/preferences/{drinkId}")
    fun updatePreferenceScore(
        @Path("drinkId") drinkId: Int,
        @Body request: DrinkPreferenceRequest
    ): Call<ApiResponse<UserDrinkPreference>>
    
    /**
     * 获取所有饮品列表
     */
    @GET("api/drinks")
    fun getAllDrinks(): Call<ApiResponse<List<Drink>>>
    
    /**
     * 根据类别获取饮品
     */
    @GET("api/drinks/category/{category}")
    fun getDrinksByCategory(
        @Path("category") category: String
    ): Call<ApiResponse<List<Drink>>>
    
    /**
     * 获取所有饮品类别
     */
    @GET("api/drinks/categories")
    fun getAllCategories(): Call<ApiResponse<List<String>>>
}

