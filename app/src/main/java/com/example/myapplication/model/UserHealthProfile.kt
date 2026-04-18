package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 用户健康档案数据模型
 */
data class UserHealthProfile(
    @SerializedName("profile_id")
    val profileId: Long? = null,
    
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: String, // male, female, other
    
    @SerializedName("height")
    val height: Float, // 身高(cm)
    
    @SerializedName("weight")
    val weight: Float, // 体重(kg)
    
    @SerializedName("health_conditions")
    val healthConditions: String? = null, // 健康状况（JSON格式）
    
    @SerializedName("allergies")
    val allergies: String? = null, // 过敏史
    
    @SerializedName("medications")
    val medications: String? = null, // 当前用药
    
    @SerializedName("activity_level")
    val activityLevel: String? = "moderate", // sedentary, light, moderate, active, very_active
    
    @SerializedName("sugar_limit")
    val sugarLimit: Float? = 50f, // 每日糖分限制(g)
    
    @SerializedName("calorie_limit")
    val calorieLimit: Float? = 2000f, // 每日热量限制(kcal)
    
    @SerializedName("water_goal")
    val waterGoal: Float? = 2000f, // 每日饮水目标(ml)
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * 健康档案创建/更新请求
 */
data class HealthProfileRequest(
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("height")
    val height: Float,
    
    @SerializedName("weight")
    val weight: Float,
    
    @SerializedName("health_conditions")
    val healthConditions: String? = null,
    
    @SerializedName("allergies")
    val allergies: String? = null,
    
    @SerializedName("medications")
    val medications: String? = null,
    
    @SerializedName("activity_level")
    val activityLevel: String? = "moderate",
    
    @SerializedName("sugar_limit")
    val sugarLimit: Float? = 50f,
    
    @SerializedName("calorie_limit")
    val calorieLimit: Float? = 2000f,
    
    @SerializedName("water_goal")
    val waterGoal: Float? = 2000f
)

/**
 * 活动水平枚举
 */
enum class ActivityLevel(val value: String, val displayName: String) {
    SEDENTARY("sedentary", "久坐不动"),
    LIGHT("light", "轻度活动"),
    MODERATE("moderate", "中度活动"),
    ACTIVE("active", "活跃"),
    VERY_ACTIVE("very_active", "非常活跃");
    
    companion object {
        fun fromValue(value: String): ActivityLevel {
            return values().find { it.value == value } ?: MODERATE
        }
    }
}


