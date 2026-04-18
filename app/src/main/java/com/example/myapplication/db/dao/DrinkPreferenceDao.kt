package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.DrinkPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkPreferenceDao {
    @Query("SELECT * FROM drink_preferences WHERE user_id = :userId")
    fun getPreferences(userId: Long): Flow<List<DrinkPreferenceEntity>>

    @Query("SELECT * FROM drink_preferences WHERE user_id = :userId AND preference_type = :type")
    fun getPreferencesByType(userId: Long, type: String): Flow<List<DrinkPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pref: DrinkPreferenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prefs: List<DrinkPreferenceEntity>)

    @Delete
    suspend fun delete(pref: DrinkPreferenceEntity)

    @Query("DELETE FROM drink_preferences WHERE user_id = :userId AND preference_type = :type")
    suspend fun deleteByType(userId: Long, type: String)

    @Query("DELETE FROM drink_preferences WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: Long)
}
