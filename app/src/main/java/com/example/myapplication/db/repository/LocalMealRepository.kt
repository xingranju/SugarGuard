package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.MealRecordDao
import com.example.myapplication.db.entity.MealRecordEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class LocalMealRepository(private val mealRecordDao: MealRecordDao) {

    fun getDailyMeals(userId: Int, date: String): Flow<List<MealRecordEntity>> =
        mealRecordDao.getDailyMeals(userId, date)

    suspend fun getDailyMealsList(userId: Int, date: String): List<MealRecordEntity> =
        mealRecordDao.getDailyMealsList(userId, date)

    fun getMealsByDateRange(userId: Int, startDate: String, endDate: String): Flow<List<MealRecordEntity>> =
        mealRecordDao.getMealsByDateRange(userId, startDate, endDate)

    suspend fun getDailySugarTotal(userId: Int, date: String): Double =
        mealRecordDao.getDailySugarTotal(userId, date)

    suspend fun getDailyCaloriesTotal(userId: Int, date: String): Double =
        mealRecordDao.getDailyCaloriesTotal(userId, date)

    suspend fun addMeal(
        userId: Int, foodName: String, sugarContent: Double, calories: Double,
        protein: Double?, fat: Double?, carbohydrate: Double?,
        portionSize: String?, notes: String?, mealType: String, imageUrl: String?
    ): Long {
        val now = Date()
        val meal = MealRecordEntity(
            userId = userId,
            mealDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
            mealTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now),
            mealType = mealType,
            foodName = foodName,
            imageUrl = imageUrl,
            sugarContent = sugarContent,
            calories = calories,
            protein = protein,
            fat = fat,
            carbohydrate = carbohydrate,
            portionSize = portionSize,
            notes = notes,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)
        )
        return mealRecordDao.insert(meal)
    }

    suspend fun deleteMeal(mealId: Int) {
        mealRecordDao.deleteById(mealId)
    }

    suspend fun getRecentDates(userId: Int, limit: Int = 30): List<String> =
        mealRecordDao.getRecentDates(userId, limit)
}
