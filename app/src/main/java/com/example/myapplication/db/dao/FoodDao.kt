package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY food_name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY food_name ASC")
    suspend fun getAllFoodsList(): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE food_id = :foodId LIMIT 1")
    suspend fun getFoodById(foodId: Int): FoodEntity?

    @Query("SELECT * FROM foods WHERE food_name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT DISTINCT category FROM foods WHERE category IS NOT NULL ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY food_name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Update
    suspend fun update(food: FoodEntity)
}
