package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PayPalRepository(private val sqlite: SQLite) {

    suspend fun getUserBonus(userId: Int): Int = withContext(Dispatchers.IO) {
        sqlite.getUserBonus(userId)
    }

    suspend fun updateUserBonus(userId: Int, bonus: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            sqlite.updateUserBonus(userId, bonus)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearCart(userId: Int) = withContext(Dispatchers.IO) {
        sqlite.clearCart(userId)
    }

    // Расчёт бонусов
    fun calculateBonuses(currentBonus: Int, usedBonus: Int, purchaseAmount: Int): Pair<Int, Int> {
        // Сначала списываем использованные бонусы (но не ниже 0)
        val bonusAfterDeduction = maxOf(0, currentBonus - usedBonus)

        // Начисляем 5% от исходной суммы покупки (до вычета бонусов)
        val bonusToAdd = (purchaseAmount * 0.05).toInt()

        return Pair(bonusAfterDeduction + bonusToAdd, bonusToAdd)
    }
}