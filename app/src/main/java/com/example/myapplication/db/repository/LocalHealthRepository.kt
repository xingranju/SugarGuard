package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.DailyHealthRecordDao
import com.example.myapplication.db.dao.UserHealthProfileDao
import com.example.myapplication.db.entity.DailyHealthRecordEntity
import com.example.myapplication.db.entity.UserHealthProfileEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class LocalHealthRepository(
    private val healthRecordDao: DailyHealthRecordDao,
    private val healthProfileDao: UserHealthProfileDao
) {
    // 健康档案
    suspend fun getProfile(userId: Long): UserHealthProfileEntity? =
        healthProfileDao.getProfile(userId)

    fun getProfileFlow(userId: Long): Flow<UserHealthProfileEntity?> =
        healthProfileDao.getProfileFlow(userId)

    suspend fun saveProfile(profile: UserHealthProfileEntity): Long {
        val existing = healthProfileDao.getProfile(profile.userId)
        return if (existing != null) {
            healthProfileDao.update(profile.copy(
                profileId = existing.profileId,
                updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
            existing.profileId
        } else {
            healthProfileDao.insert(profile.copy(
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
        }
    }

    // 每日健康记录
    suspend fun getTodayRecord(userId: Long): DailyHealthRecordEntity? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return healthRecordDao.getRecord(userId, today)
    }

    suspend fun getRecord(userId: Long, date: String): DailyHealthRecordEntity? =
        healthRecordDao.getRecord(userId, date)

    fun getRecordFlow(userId: Long, date: String): Flow<DailyHealthRecordEntity?> =
        healthRecordDao.getRecordFlow(userId, date)

    fun getRecentRecords(userId: Long, limit: Int = 30): Flow<List<DailyHealthRecordEntity>> =
        healthRecordDao.getRecentRecords(userId, limit)

    fun getRecordsByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<DailyHealthRecordEntity>> =
        healthRecordDao.getRecordsByDateRange(userId, startDate, endDate)

    suspend fun saveRecord(record: DailyHealthRecordEntity): Long {
        val existing = healthRecordDao.getRecord(record.userId, record.recordDate)
        return if (existing != null) {
            healthRecordDao.update(record.copy(recordId = existing.recordId))
            existing.recordId
        } else {
            healthRecordDao.insert(record.copy(
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
        }
    }
}
