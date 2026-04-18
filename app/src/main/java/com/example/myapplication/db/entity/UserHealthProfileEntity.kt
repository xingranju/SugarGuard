package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_health_profiles")
data class UserHealthProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "profile_id")
    val profileId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    @ColumnInfo(name = "health_conditions")
    val healthConditions: String? = null,
    val allergies: String? = null,
    val medications: String? = null,
    @ColumnInfo(name = "activity_level")
    val activityLevel: String? = "moderate",
    @ColumnInfo(name = "sugar_limit")
    val sugarLimit: Float? = 25f,
    @ColumnInfo(name = "calorie_limit")
    val calorieLimit: Float? = 2000f,
    @ColumnInfo(name = "water_goal")
    val waterGoal: Float? = 2000f,
    @ColumnInfo(name = "bmi")
    val bmi: Float? = null,
    @ColumnInfo(name = "blood_type")
    val bloodType: String? = null,
    @ColumnInfo(name = "diabetes_type")
    val diabetesType: String? = null,
    @ColumnInfo(name = "target_weight")
    val targetWeight: Float? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)
