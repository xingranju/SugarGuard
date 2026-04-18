package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * 每日健康记录数据模型
 */
data class DailyHealthRecord(
    @SerializedName("record_id")
    val recordId: Long? = null,
    
    @SerializedName("user_id")
    val userId: Long,
    
    @SerializedName("record_date")
    val recordDate: String?, // yyyy-MM-dd格式
    
    @SerializedName("total_sugar_intake")
    val totalSugarIntake: Float? = 0f,
    
    @SerializedName("total_calories")
    val totalCalories: Float? = 0f,
    
    @SerializedName("water_intake")
    val waterIntake: Float? = 0f,
    
    @SerializedName("exercise_minutes")
    val exerciseMinutes: Float? = 0f,
    
    @SerializedName("sleep_hours")
    val sleepHours: Float? = null,
    
    @SerializedName("systolic_bp")
    val systolicBp: Float? = null, // 收缩压
    
    @SerializedName("diastolic_bp")
    val diastolicBp: Float? = null, // 舒张压
    
    @SerializedName("blood_glucose")
    val bloodGlucose: Float? = null, // 血糖
    
    @SerializedName("weight")
    val weight: Float? = null,
    
    @SerializedName("mood")
    val mood: String? = null, // excellent, good, normal, bad, terrible
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * 健康记录创建/更新请求
 */
data class HealthRecordRequest(
    @SerializedName("record_date")
    val recordDate: String, // yyyy-MM-dd
    
    @SerializedName("total_sugar_intake")
    val totalSugarIntake: Float? = null,
    
    @SerializedName("total_calories")
    val totalCalories: Float? = null,
    
    @SerializedName("water_intake")
    val waterIntake: Float? = null,
    
    @SerializedName("exercise_minutes")
    val exerciseMinutes: Float? = null,
    
    @SerializedName("sleep_hours")
    val sleepHours: Float? = null,
    
    @SerializedName("systolic_bp")
    val systolicBp: Float? = null,
    
    @SerializedName("diastolic_bp")
    val diastolicBp: Float? = null,
    
    @SerializedName("blood_glucose")
    val bloodGlucose: Float? = null,
    
    @SerializedName("weight")
    val weight: Float? = null,
    
    @SerializedName("mood")
    val mood: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * 心情枚举
 */
enum class Mood(val value: String, val displayName: String, val emoji: String) {
    EXCELLENT("excellent", "极好", "😄"),
    GOOD("good", "良好", "😊"),
    NORMAL("normal", "一般", "😐"),
    BAD("bad", "较差", "😟"),
    TERRIBLE("terrible", "很差", "😢");
    
    companion object {
        fun fromValue(value: String?): Mood {
            return values().find { it.value == value } ?: NORMAL
        }
    }
}

