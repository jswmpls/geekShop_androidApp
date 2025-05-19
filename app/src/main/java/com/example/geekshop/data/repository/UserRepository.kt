package com.example.geekshop.repository

import android.content.ContentValues
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val sqlite: SQLite) {
    // Добавление нового пользователя
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

    // Проверка существования логина
    suspend fun isLoginExists(login: String): Boolean = withContext(Dispatchers.IO) {
        sqlite.isLoginExists(login)
    }

    // Получение следующего ID для нового пользователя
    suspend fun getNextUserId(): Int = withContext(Dispatchers.IO) {
        sqlite.getNextUserId()
    }

    // Авторизация пользователя
    suspend fun authenticateUser(login: String, password: String): Users? = withContext(Dispatchers.IO) {
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