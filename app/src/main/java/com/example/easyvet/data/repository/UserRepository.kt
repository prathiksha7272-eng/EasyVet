package com.example.easyvet.data.repository

import com.example.easyvet.data.local.SeedData
import com.example.easyvet.data.local.dao.UserDao
import com.example.easyvet.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.login(email.trim(), password)
    }

    suspend fun register(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(user.email.trim())
        if (existing != null) {
            false // User already exists
        } else {
            userDao.insertUser(user)
            true
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email.trim())
    }

    suspend fun ensureSeedData() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            for (user in SeedData.initialUsers) {
                userDao.insertUser(user)
            }
        }
    }
}
