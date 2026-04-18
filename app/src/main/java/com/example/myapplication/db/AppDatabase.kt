package com.example.myapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.db.dao.*
import com.example.myapplication.db.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DrinkEntity::class,
        FoodEntity::class,
        MealRecordEntity::class,
        DailyHealthRecordEntity::class,
        UserHealthProfileEntity::class,
        DrinkPreferenceEntity::class,
        ConversationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun drinkDao(): DrinkDao
    abstract fun foodDao(): FoodDao
    abstract fun mealRecordDao(): MealRecordDao
    abstract fun dailyHealthRecordDao(): DailyHealthRecordDao
    abstract fun userHealthProfileDao(): UserHealthProfileDao
    abstract fun drinkPreferenceDao(): DrinkPreferenceDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sugar_guard_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DatabaseSeeder.seedAll(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
