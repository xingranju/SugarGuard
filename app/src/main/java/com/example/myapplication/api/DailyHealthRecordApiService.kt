package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.DailyHealthRecord
import com.example.myapplication.model.HealthRecordRequest
import retrofit2.Call
import retrofit2.http.*

/**
 * 每日健康记录API服务接口
 */
interface DailyHealthRecordApiService {
    
    /**
     * 获取指定日期的健康记录
     */
    @GET("api/health-records/{date}")
    fun getRecordByDate(
        @Path("date") date: String
    ): Call<ApiResponse<DailyHealthRecord>>
    
    /**
     * 获取最近N天的健康记录
     */
    @GET("api/health-records/recent/{days}")
    fun getRecentRecords(
        @Path("days") days: Int
    ): Call<ApiResponse<List<DailyHealthRecord>>>
    
    /**
     * 获取指定日期范围的健康记录
     */
    @GET("api/health-records/range")
    fun getRecordsByDateRange(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Call<ApiResponse<List<DailyHealthRecord>>>
    
    /**
     * 创建或更新健康记录
     */
    @POST("api/health-records")
    fun createOrUpdateRecord(
        @Body request: HealthRecordRequest
    ): Call<ApiResponse<DailyHealthRecord>>
    
    /**
     * 删除健康记录
     */
    @DELETE("api/health-records/{date}")
    fun deleteRecord(
        @Path("date") date: String
    ): Call<ApiResponse<Void>>
    
    /**
     * 获取记录统计
     */
    @GET("api/health-records/stats/count")
    fun getRecordCount(): Call<ApiResponse<Long>>
}


