package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_preferences")
data class DrinkPreferenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "preference_type")
    val preferenceType: String,
    @ColumnInfo(name = "preference_value")
    val preferenceValue: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
