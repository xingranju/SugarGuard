package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.MealRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealRecordDao {
    @Query("SELECT * FROM meal_records WHERE user_id = :userId AND meal_date = :date ORDER BY meal_time ASC")
    fun getDailyMeals(userId: Int, date: String): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE user_id = :userId AND meal_date = :date ORDER BY meal_time ASC")
    suspend fun getDailyMealsList(userId: Int, date: String): List<MealRecordEntity>

    @Query("SELECT * FROM meal_records WHERE user_id = :userId AND meal_date BETWEEN :startDate AND :endDate ORDER BY meal_date DESC, meal_time ASC")
    fun getMealsByDateRange(userId: Int, startDate: String, endDate: String): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE meal_id = :mealId LIMIT 1")
    suspend fun getMealById(mealId: Int): MealRecordEntity?

    @Query("SELECT COALESCE(SUM(sugar_content), 0) FROM meal_records WHERE user_id = :userId AND meal_date = :date")
    suspend fun getDailySugarTotal(userId: Int, date: String): Double

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meal_records WHERE user_id = :userId AND meal_date = :date")
    suspend fun getDailyCaloriesTotal(userId: Int, date: String): Double

    @Query("SELECT COUNT(*) FROM meal_records WHERE user_id = :userId AND meal_date = :date")
    suspend fun getDailyMealCount(userId: Int, date: String): Int

    @Query("SELECT DISTINCT meal_date FROM meal_records WHERE user_id = :userId ORDER BY meal_date DESC LIMIT :limit")
    suspend fun getRecentDates(userId: Int, limit: Int = 30): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealRecordEntity): Long

    @Update
    suspend fun update(meal: MealRecordEntity)

    @Query("DELETE FROM meal_records WHERE meal_id = :mealId")
    suspend fun deleteById(mealId: Int)

    @Delete
    suspend fun delete(meal: MealRecordEntity)
}
