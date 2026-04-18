package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "food_id")
    val foodId: Int = 0,
    @ColumnInfo(name = "food_name")
    val foodName: String,
    val category: String? = null,
    @ColumnInfo(name = "sugar_content")
    val sugarContent: Float = 0f,
    val calories: Float = 0f,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbohydrate: Float? = null,
    val fiber: Float? = null,
    val sodium: Float? = null,
    @ColumnInfo(name = "serving_size")
    val servingSize: String? = null,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "health_tips")
    val healthTips: String? = null,
    @ColumnInfo(name = "gi_value")
    val giValue: Int? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
