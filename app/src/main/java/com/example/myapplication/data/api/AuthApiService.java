package com.example.myapplication.data.api;

import com.example.myapplication.data.model.ApiResponse;
import com.example.myapplication.data.model.LoginRequest;
import com.example.myapplication.data.model.LoginResponse;
import com.example.myapplication.data.model.RegisterRequest;
import com.example.myapplication.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 认证API服务接口
 * renZheng_API_fuWu
 */
public interface AuthApiService {

    /**
     * 用户登录
     * yongHu_dengLu
     */
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    /**
     * 用户注册
     * yongHu_zhuCe
     */
    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest registerRequest);

    /**
     * 刷新token
     * shuaXin_token
     */
    @POST("auth/refresh")
    Call<ApiResponse<LoginResponse>> refreshToken(@Body String refreshToken);

    /**
     * 登出
     * dengChu
     */
    @POST("auth/logout")
    Call<ApiResponse<Void>> logout();

    /**
     * 获取当前用户信息
     * huoQu_dangQian_yongHu_xinXi
     */
    @POST("auth/me")
    Call<ApiResponse<User>> getCurrentUser();
}
