package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.FoodDao
import com.example.myapplication.db.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

class LocalFoodRepository(private val foodDao: FoodDao) {

    fun getAllFoods(): Flow<List<FoodEntity>> = foodDao.getAllFoods()

    suspend fun getAllFoodsList(): List<FoodEntity> = foodDao.getAllFoodsList()

    suspend fun getFoodById(foodId: Int): FoodEntity? = foodDao.getFoodById(foodId)

    fun searchFoods(query: String): Flow<List<FoodEntity>> = foodDao.searchFoods(query)

    suspend fun getAllCategories(): List<String> = foodDao.getAllCategories()

    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>> =
        foodDao.getFoodsByCategory(category)
}
