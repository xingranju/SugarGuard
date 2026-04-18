package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "drink_id")
    val drinkId: Int = 0,
    @ColumnInfo(name = "drink_name")
    val drinkName: String,
    val brand: String? = null,
    val category: String? = null,
    @ColumnInfo(name = "sugar_content")
    val sugarContent: Float? = null,
    val calories: Float? = null,
    val volume: Float? = 500f,
    val caffeine: Float? = 0f,
    val fat: Float? = 0f,
    val protein: Float? = 0f,
    val sodium: Float? = 0f,
    @ColumnInfo(name = "health_score")
    val healthScore: Int? = 50,
    val ingredients: String? = null,
    val allergens: String? = null,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)
