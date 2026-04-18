package com.example.myapplication.db.dao

import androidx.room.*
import com.example.myapplication.db.entity.DailyHealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHealthRecordDao {
    @Query("SELECT * FROM daily_health_records WHERE user_id = :userId AND record_date = :date LIMIT 1")
    suspend fun getRecord(userId: Long, date: String): DailyHealthRecordEntity?

    @Query("SELECT * FROM daily_health_records WHERE user_id = :userId AND record_date = :date LIMIT 1")
    fun getRecordFlow(userId: Long, date: String): Flow<DailyHealthRecordEntity?>

    @Query("SELECT * FROM daily_health_records WHERE user_id = :userId ORDER BY record_date DESC LIMIT :limit")
    fun getRecentRecords(userId: Long, limit: Int = 30): Flow<List<DailyHealthRecordEntity>>

    @Query("SELECT * FROM daily_health_records WHERE user_id = :userId AND record_date BETWEEN :startDate AND :endDate ORDER BY record_date ASC")
    fun getRecordsByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<DailyHealthRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DailyHealthRecordEntity): Long

    @Update
    suspend fun update(record: DailyHealthRecordEntity)

    @Query("DELETE FROM daily_health_records WHERE record_id = :recordId")
    suspend fun deleteById(recordId: Long)
}
