package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 用户饮品偏好数据模型
 */
data class UserDrinkPreference(
    @SerializedName("preference_id")
    val preferenceId: Long? = null,
    
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("drink_id")
    val drinkId: Int = -1,  // 使用-1作为无效ID的标记,匹配后端snake_case
    
    @SerializedName("drink_name")
    val drinkName: String? = null,
    
    @SerializedName("brand")
    val brand: String? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("sugar_content")
    val sugarContent: Float? = null,
    
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("health_score")
    val healthScore: Int? = null,
    
    @SerializedName("preference_score")
    val preferenceScore: Int? = 3, // 偏好评分(1-5)
    
    @SerializedName("last_consumed")
    val lastConsumed: String? = null,
    
    @SerializedName("times_consumed")
    val timesConsumed: Int? = 0,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * 添加/更新饮品偏好请求
 */
data class DrinkPreferenceRequest(
    @SerializedName("drinkId")
    val drinkId: Int,
    
    @SerializedName("preferenceScore")
    val preferenceScore: Int? = 3
)

/**
 * 饮品类别
 */
enum class DrinkCategory(val value: String, val displayName: String) {
    TEA("茶饮", "茶饮"),
    COFFEE("咖啡", "咖啡"),
    MILK_TEA("奶茶", "奶茶"),
    JUICE("果汁", "果汁"),
    SODA("碳酸饮料", "碳酸饮料"),
    SPORTS("运动饮料", "运动饮料"),
    DAIRY("乳制品", "乳制品"),
    WATER("水", "水");
    
    companion object {
        fun fromValue(value: String): DrinkCategory? {
            return values().find { it.value == value }
        }
    }
}

