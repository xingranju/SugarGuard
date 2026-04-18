package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.DrinkDao
import com.example.myapplication.db.entity.DrinkEntity
import kotlinx.coroutines.flow.Flow

class LocalDrinkRepository(private val drinkDao: DrinkDao) {

    fun getAllDrinks(): Flow<List<DrinkEntity>> = drinkDao.getAllDrinks()

    suspend fun getAllDrinksList(): List<DrinkEntity> = drinkDao.getAllDrinksList()

    suspend fun getDrinkById(drinkId: Int): DrinkEntity? = drinkDao.getDrinkById(drinkId)

    fun searchDrinks(query: String): Flow<List<DrinkEntity>> = drinkDao.searchDrinks(query)

    suspend fun getAllBrands(): List<String> = drinkDao.getAllBrands()

    suspend fun getAllCategories(): List<String> = drinkDao.getAllCategories()

    fun getDrinksByCategory(category: String): Flow<List<DrinkEntity>> =
        drinkDao.getDrinksByCategory(category)

    fun getDrinksByBrand(brand: String): Flow<List<DrinkEntity>> =
        drinkDao.getDrinksByBrand(brand)

    suspend fun getDrinkCount(): Int = drinkDao.getDrinkCount()
}
