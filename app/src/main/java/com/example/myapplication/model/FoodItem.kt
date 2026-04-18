package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class FoodItem(
    val id: Long? = null,
    @SerializedName("nameCN") val nameCN: String? = null,
    @SerializedName("nameEN") val nameEN: String? = null,
    val category: String? = null,
    val sugar: Float? = 0f,
    val calories: Float? = 0f,
    val protein: Float? = 0f,
    val fat: Float? = 0f,
    val carbohydrate: Float? = 0f,
    @SerializedName("servingSize") val servingSize: Float? = 0f,
    @SerializedName("healthLevel") val healthLevel: String? = null,
    @SerializedName("healthAdvice") val healthAdvice: String? = null,
    val confidence: Float? = 0f,
    @SerializedName("imageUrl") val imageUrl: String? = null
)
