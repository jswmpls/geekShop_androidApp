// File: ProfileRepository.kt
package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val sqlite: SQLite) {

    suspend fun getUserBonus(userId: Int): Int = withContext(Dispatchers.IO) {
        sqlite.getUserBonus(userId)
    }
}