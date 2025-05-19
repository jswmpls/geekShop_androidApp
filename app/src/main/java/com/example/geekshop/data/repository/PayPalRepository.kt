package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с платежами через PayPal
 */
class PayPalRepository(private val sqlite: SQLite) {

    /**
     * Получает текущее количество бонусов пользователя
     */
    suspend fun getUserBonus(userId: Int): Int = withContext(Dispatchers.IO) {
        sqlite.getUserBonus(userId)
    }

    /**
     * Обновляет количество бонусов пользователя
     * @return true если обновление прошло успешно
     */
    suspend fun updateUserBonus(userId: Int, bonus: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            sqlite.updateUserBonus(userId, bonus)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Очищает корзину пользователя после успешной оплаты
     */
    suspend fun clearCart(userId: Int) = withContext(Dispatchers.IO) {
        sqlite.clearCart(userId)
    }

    /**
     * Рассчитывает новые бонусы после покупки
     * @param currentBonus Текущие бонусы
     * @param usedBonus Использованные бонусы
     * @param purchaseAmount Сумма покупки
     * @return Пара: (новый баланс бонусов, начисленные бонусы)
     */
    fun calculateBonuses(currentBonus: Int, usedBonus: Int, purchaseAmount: Int): Pair<Int, Int> {
        // 1. Списываем использованные бонусы (не ниже 0)
        val bonusAfterDeduction = maxOf(0, currentBonus - usedBonus)

        // 2. Начисляем 5% от суммы покупки
        val bonusToAdd = (purchaseAmount * 0.05).toInt()

        return bonusAfterDeduction + bonusToAdd to bonusToAdd
    }
}