package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.UserHealthProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserHealthProfileDao {
    @Query("SELECT * FROM user_health_profiles WHERE user_id = :userId LIMIT 1")
    suspend fun getProfile(userId: Long): UserHealthProfileEntity?

    @Query("SELECT * FROM user_health_profiles WHERE user_id = :userId LIMIT 1")
    fun getProfileFlow(userId: Long): Flow<UserHealthProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserHealthProfileEntity): Long

    @Update
    suspend fun update(profile: UserHealthProfileEntity)

    @Query("DELETE FROM user_health_profiles WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long)
}
