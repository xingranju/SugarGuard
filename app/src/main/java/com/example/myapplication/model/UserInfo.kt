package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 用户信息数据类
 */
data class UserInfo(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    
    @SerializedName("gender")
    val gender: String?, // male, female, other
    
    @SerializedName("birthday")
    val birthday: String?, // yyyy-MM-dd
    
    @SerializedName("status")
    val status: String?,
    
    @SerializedName("email_verified")
    val emailVerified: Boolean?,
    
    @SerializedName("phone_verified")
    val phoneVerified: Boolean?,
    
    @SerializedName("last_login_time")
    val lastLoginTime: String?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * 更新用户信息请求
 */
data class UpdateUserInfoRequest(
    @SerializedName("username")
    val username: String?,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    
    @SerializedName("gender")
    val gender: String?, // male, female, other
    
    @SerializedName("birthday")
    val birthday: String? // yyyy-MM-dd
)


