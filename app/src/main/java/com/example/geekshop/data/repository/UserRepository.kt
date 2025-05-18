package com.example.geekshop.repository

import android.content.ContentValues
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val sqlite: SQLite) {

    // Method to add a new user
    suspend fun addUser(user: Users): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("id", user.id)
            put("name", user.name)
            put("login", user.login)
            put("password", user.password)
            put("bonus", user.bonus)
        }
        sqlite.writableDatabase.insert("users", null, values)
    }

    // Method to check if login exists
    suspend fun isLoginExists(login: String): Boolean {
        return withContext(Dispatchers.IO) {
            sqlite.isLoginExists(login)
        }
    }

    // Method to get next user ID
    suspend fun getNextUserId(): Int {
        return withContext(Dispatchers.IO) {
            sqlite.getNextUserId()
        }
    }

    // Method to authenticate a user
    suspend fun authenticateUser(login: String, password: String): Users? {
        return withContext(Dispatchers.IO) {
            try {
                val user = sqlite.getUserData(login)
                if (sqlite.getUser(login, password)) {
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}