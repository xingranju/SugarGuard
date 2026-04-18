package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.DrinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drinks ORDER BY drink_name ASC")
    fun getAllDrinks(): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks ORDER BY drink_name ASC")
    suspend fun getAllDrinksList(): List<DrinkEntity>

    @Query("SELECT * FROM drinks WHERE drink_id = :drinkId LIMIT 1")
    suspend fun getDrinkById(drinkId: Int): DrinkEntity?

    @Query("SELECT * FROM drinks WHERE drink_name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchDrinks(query: String): Flow<List<DrinkEntity>>

    @Query("SELECT DISTINCT brand FROM drinks WHERE brand IS NOT NULL ORDER BY brand ASC")
    suspend fun getAllBrands(): List<String>

    @Query("SELECT DISTINCT category FROM drinks WHERE category IS NOT NULL ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM drinks WHERE category = :category ORDER BY drink_name ASC")
    fun getDrinksByCategory(category: String): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks WHERE brand = :brand ORDER BY drink_name ASC")
    fun getDrinksByBrand(brand: String): Flow<List<DrinkEntity>>

    @Query("SELECT COUNT(*) FROM drinks")
    suspend fun getDrinkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drink: DrinkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drinks: List<DrinkEntity>)

    @Update
    suspend fun update(drink: DrinkEntity)

    @Delete
    suspend fun delete(drink: DrinkEntity)
}
