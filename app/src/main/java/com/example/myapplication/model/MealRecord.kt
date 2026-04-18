package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * 饮食日记记录
 */
data class MealRecord(
    @SerializedName("meal_id")
    val mealId: Int? = null,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("meal_date")
    val mealDate: String, // 格式: yyyy-MM-dd
    
    @SerializedName("meal_time")
    val mealTime: String, // 格式: HH:mm:ss
    
    @SerializedName("meal_type")
    val mealType: String, // breakfast, lunch, dinner, snack
    
    @SerializedName("drink_id")
    val drinkId: Int? = null,
    
    @SerializedName("food_name")
    val foodName: String,
    
    @SerializedName("image_path")
    val foodImagePath: String? = null,
    
    @SerializedName("sugar_content")
    val sugarContent: Double, // 克
    
    @SerializedName("calories")
    val calories: Double, // 卡路里
    
    @SerializedName("protein")
    val protein: Double? = null, // 蛋白质(克)
    
    @SerializedName("fat")
    val fat: Double? = null, // 脂肪(克)
    
    @SerializedName("carbohydrate")
    val carbohydrate: Double? = null, // 碳水化合物(克)
    
    @SerializedName("portion_size")
    val portionSize: String? = null, // 份量描述
    
    @SerializedName("notes")
    val notes: String? = null, // 备注
    
    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * 每日饮食记录汇总
 */
data class DailyMealSummary(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("meals")
    val meals: List<MealRecord>,
    
    @SerializedName("total_sugar")
    val totalSugar: Double,
    
    @SerializedName("total_calories")
    val totalCalories: Double,
    
    @SerializedName("meal_count")
    val mealCount: Int
)

/**
 * 添加饮食记录请求
 */
data class AddMealRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("meal_date")
    val mealDate: String,
    
    @SerializedName("meal_time")
    val mealTime: String,
    
    @SerializedName("meal_type")
    val mealType: String,
    
    @SerializedName("food_name")
    val foodName: String,
    
    @SerializedName("sugar_content")
    val sugarContent: Double,
    
    @SerializedName("calories")
    val calories: Double,
    
    @SerializedName("protein")
    val protein: Double? = null,
    
    @SerializedName("fat")
    val fat: Double? = null,
    
    @SerializedName("carbohydrate")
    val carbohydrate: Double? = null,
    
    @SerializedName("portion_size")
    val portionSize: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("image_path")
    val imagePath: String? = null
)

/**
 * 餐次类型
 */
enum class MealType(val value: String, val displayName: String) {
    BREAKFAST("breakfast", "早餐"),
    LUNCH("lunch", "午餐"),
    DINNER("dinner", "晚餐"),
    SNACK("snack", "加餐");
    
    companion object {
        fun fromValue(value: String): MealType {
            return values().find { it.value == value } ?: SNACK
        }
        
        fun getCurrentMealType(): MealType {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return when {
                hour in 6..10 -> BREAKFAST
                hour in 11..13 -> LUNCH
                hour in 17..20 -> DINNER
                else -> SNACK
            }
        }
    }
}

