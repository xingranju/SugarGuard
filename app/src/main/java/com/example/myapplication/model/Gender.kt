package com.example.myapplication.model

/**
 * 性别枚举
 */
enum class Gender(val value: String, val displayName: String) {
    MALE("male", "男"),
    FEMALE("female", "女"),
    OTHER("other", "其他");
    
    companion object {
        fun fromValue(value: String?): Gender {
            return values().find { it.value == value } ?: OTHER
        }
    }
}

