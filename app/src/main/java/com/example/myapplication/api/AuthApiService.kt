package com.example.myapplication.api

import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.RegisterRequest
import com.example.myapplication.model.UserDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<LoginResponse>>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<UserDto>>

    @POST("api/auth/me")
    fun getCurrentUser(): Call<ApiResponse<UserDto>>

    @POST("api/auth/logout")
    fun logout(): Call<ApiResponse<Void>>
}
