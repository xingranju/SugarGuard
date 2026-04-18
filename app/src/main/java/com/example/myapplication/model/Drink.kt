package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 饮品数据模型
 */
data class Drink(
    @SerializedName("drink_id")
    val drinkId: Int = -1,  // 使用-1作为无效ID的标记
    
    @SerializedName("drink_name")
    val drinkName: String?,
    
    @SerializedName("brand")
    val brand: String?,
    
    @SerializedName("category")
    val category: String?,
    
    @SerializedName("sugar_content")
    val sugarContent: Float?,
    
    @SerializedName("calories")
    val calories: Float?,
    
    @SerializedName("volume")
    val volume: Float? = 500f,
    
    @SerializedName("caffeine")
    val caffeine: Float? = 0f,
    
    @SerializedName("fat")
    val fat: Float? = 0f,
    
    @SerializedName("protein")
    val protein: Float? = 0f,
    
    @SerializedName("sodium")
    val sodium: Float? = 0f,
    
    @SerializedName("health_score")
    val healthScore: Int? = 50,
    
    @SerializedName("ingredients")
    val ingredients: String?,
    
    @SerializedName("allergens")
    val allergens: String?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("source_url")
    val sourceUrl: String?,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * 添加饮品记录请求
 */
data class AddDrinkRecordRequest(
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("drink_id")
    val drinkId: Int,
    
    @SerializedName("meal_type")
    val mealType: String,
    
    @SerializedName("portion_size")
    val portionSize: Float? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * 饮品统计信息
 */
data class DrinkStatistics(
    @SerializedName("total_drinks")
    val totalDrinks: Long,
    
    @SerializedName("total_brands")
    val totalBrands: Int,
    
    @SerializedName("total_categories")
    val totalCategories: Int,
    
    @SerializedName("brands")
    val brands: List<String>,
    
    @SerializedName("categories")
    val categories: List<String>
)

