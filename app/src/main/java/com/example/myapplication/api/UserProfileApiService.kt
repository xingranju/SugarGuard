package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.HealthProfileRequest
import com.example.myapplication.model.UserHealthProfile
import retrofit2.Call
import retrofit2.http.*

/**
 * 用户健康档案API服务接口
 */
interface UserProfileApiService {
    
    /**
     * 获取当前用户的健康档案
     */
    @GET("api/profile/health")
    fun getHealthProfile(): Call<ApiResponse<UserHealthProfile>>
    
    /**
     * 创建或更新健康档案
     */
    @POST("api/profile/health")
    fun createOrUpdateHealthProfile(
        @Body request: HealthProfileRequest
    ): Call<ApiResponse<UserHealthProfile>>
    
    /**
     * 删除健康档案
     */
    @DELETE("api/profile/health")
    fun deleteHealthProfile(): Call<ApiResponse<Void>>
    
    /**
     * 计算BMI
     */
    @GET("api/profile/health/bmi")
    fun calculateBMI(): Call<ApiResponse<Double>>
}


