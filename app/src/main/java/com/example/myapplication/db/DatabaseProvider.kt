package com.example.myapplication.db

import android.content.Context
import com.example.myapplication.db.repository.*

object DatabaseProvider {
    private var database: AppDatabase? = null

    fun init(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
        }
    }

    fun getDatabase(): AppDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }

    val userRepository: LocalUserRepository by lazy {
        LocalUserRepository(getDatabase().userDao())
    }

    val drinkRepository: LocalDrinkRepository by lazy {
        LocalDrinkRepository(getDatabase().drinkDao())
    }

    val foodRepository: LocalFoodRepository by lazy {
        LocalFoodRepository(getDatabase().foodDao())
    }

    val mealRepository: LocalMealRepository by lazy {
        LocalMealRepository(getDatabase().mealRecordDao())
    }

    val healthRepository: LocalHealthRepository by lazy {
        LocalHealthRepository(
            getDatabase().dailyHealthRecordDao(),
            getDatabase().userHealthProfileDao()
        )
    }

    val conversationRepository: LocalConversationRepository by lazy {
        LocalConversationRepository(getDatabase().conversationDao())
    }
}
