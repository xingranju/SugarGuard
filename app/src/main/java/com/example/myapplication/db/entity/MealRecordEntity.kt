package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_records")
data class MealRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "meal_id")
    val mealId: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "meal_date")
    val mealDate: String,
    @ColumnInfo(name = "meal_time")
    val mealTime: String,
    @ColumnInfo(name = "meal_type")
    val mealType: String,
    @ColumnInfo(name = "drink_id")
    val drinkId: Int? = null,
    @ColumnInfo(name = "food_id")
    val foodId: Int? = null,
    @ColumnInfo(name = "food_name")
    val foodName: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "sugar_content")
    val sugarContent: Double,
    val calories: Double,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbohydrate: Double? = null,
    @ColumnInfo(name = "portion_size")
    val portionSize: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
