package com.example.myapplication.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_health_records")
data class DailyHealthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    val recordId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "record_date")
    val recordDate: String,
    @ColumnInfo(name = "total_sugar_intake")
    val totalSugarIntake: Float? = 0f,
    @ColumnInfo(name = "total_calories")
    val totalCalories: Float? = 0f,
    @ColumnInfo(name = "water_intake")
    val waterIntake: Float? = 0f,
    @ColumnInfo(name = "exercise_minutes")
    val exerciseMinutes: Float? = 0f,
    @ColumnInfo(name = "sleep_hours")
    val sleepHours: Float? = null,
    @ColumnInfo(name = "systolic_bp")
    val systolicBp: Float? = null,
    @ColumnInfo(name = "diastolic_bp")
    val diastolicBp: Float? = null,
    @ColumnInfo(name = "blood_glucose")
    val bloodGlucose: Float? = null,
    val weight: Float? = null,
    val mood: String? = null,
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
