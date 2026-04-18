package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.UpdateUserInfoRequest
import com.example.myapplication.model.UserInfo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

/**
 * 用户信息API服务接口
 */
interface UserInfoApiService {
    
    /**
     * 获取当前用户信息
     */
    @GET("api/user/info")
    fun getUserInfo(): Call<ApiResponse<UserInfo>>
    
    /**
     * 更新用户信息
     */
    @PUT("api/user/info")
    fun updateUserInfo(@Body request: UpdateUserInfoRequest): Call<ApiResponse<UserInfo>>
    
    /**
     * 上传用户头像
     */
    @Multipart
    @POST("api/user/avatar")
    fun uploadAvatar(@Part file: MultipartBody.Part): Call<ApiResponse<String>>
}


