package com.example.myapplication.api

import com.example.myapplication.model.AddDrinkRecordRequest
import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.Drink
import com.example.myapplication.model.DrinkStatistics
import com.example.myapplication.model.MealRecord
import retrofit2.Call
import retrofit2.http.*

/**
 * 饮品API服务接口
 */
interface DrinkApiService {
    
    /**
     * 获取所有饮品
     */
    @GET("api/drinks")
    fun getAllDrinks(): Call<ApiResponse<List<Drink>>>
    
    /**
     * 根据ID获取饮品详情
     */
    @GET("api/drinks/{drinkId}")
    fun getDrinkById(@Path("drinkId") drinkId: Int): Call<ApiResponse<Drink>>
    
    /**
     * 搜索饮品
     * @param keyword 关键词（名称模糊搜索）
     * @param brand 品牌
     * @param category 类别
     */
    @GET("api/drinks/search")
    fun searchDrinks(
        @Query("keyword") keyword: String? = null,
        @Query("brand") brand: String? = null,
        @Query("category") category: String? = null
    ): Call<ApiResponse<List<Drink>>>
    
    /**
     * 获取所有品牌
     */
    @GET("api/drinks/brands")
    fun getAllBrands(): Call<ApiResponse<List<String>>>
    
    /**
     * 获取所有类别
     */
    @GET("api/drinks/categories")
    fun getAllCategories(): Call<ApiResponse<List<String>>>
    
    /**
     * 获取饮品统计信息
     */
    @GET("api/drinks/statistics")
    fun getDrinkStatistics(): Call<ApiResponse<DrinkStatistics>>
    
    /**
     * 手动添加饮品记录
     */
    @POST("api/drinks/add-record")
    fun addDrinkRecord(@Body request: AddDrinkRecordRequest): Call<ApiResponse<MealRecord>>
}

