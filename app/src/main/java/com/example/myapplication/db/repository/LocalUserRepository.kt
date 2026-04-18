package com.example.myapplication.db.repository

import com.example.myapplication.db.dao.UserDao
import com.example.myapplication.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val userDao: UserDao) {

    suspend fun login(username: String, password: String): UserEntity? {
        return userDao.login(username, password)
    }

    suspend fun register(username: String, password: String, email: String): Result<UserEntity> {
        val existing = userDao.findByUsername(username)
        if (existing != null) {
            return Result.failure(Exception("用户名已存在"))
        }
        val user = UserEntity(
            username = username,
            password = password,
            email = email,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
        val id = userDao.insert(user)
        return Result.success(user.copy(id = id))
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdFlow(userId: Long): Flow<UserEntity?> {
        return userDao.getUserByIdFlow(userId)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }
}
