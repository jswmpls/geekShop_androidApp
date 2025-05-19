package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с корзиной товаров
 */
class CartRepository(private val sqlite: SQLite) {

    /**
     * Получение товаров в корзине пользователя
     * @param userId ID пользователя
     * @return Список пар (товар, количество)
     */
    fun getCartProducts(userId: Int): List<Pair<Products, Int>> {
        return sqlite.getCartProducts(userId)
    }

    /**
     * Удаление товара из корзины пользователя
     * @param userId ID пользователя
     * @param productId ID товара
     */
    suspend fun removeFromCart(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        sqlite.removeFromCart(userId, productId)
    }

    /**
     * Очистка корзины пользователя
     * @param userId ID пользователя
     */
    suspend fun clearCart(userId: Int) = withContext(Dispatchers.IO) {
        sqlite.clearCart(userId)
    }

    /**
     * Получение количества бонусов пользователя
     * @param userId ID пользователя
     * @return Количество бонусов
     */
    fun getUserBonus(userId: Int): Int {
        return sqlite.getUserBonus(userId)
    }

    /**
     * Обновление количества бонусов пользователя
     * @param userId ID пользователя
     * @param newBonus Новое количество бонусов
     */
    suspend fun updateUserBonus(userId: Int, newBonus: Int) = withContext(Dispatchers.IO) {
        sqlite.updateUserBonus(userId, newBonus)
    }

    /**
     * Расчёт общей суммы заказа
     * @param cartItems Список товаров в корзине с количеством
     * @param days Количество дней аренды
     * @return Общая сумма заказа
     */
    fun calculateTotalSum(cartItems: List<Pair<Products, Int>>, days: Int): Int {
        var totalSum = cartItems.sumOf { (product, quantity) ->
            product.Cost.replace(" ₽", "").toIntOrNull() ?: 0 * quantity
        }
        totalSum += days * 200 // 200 ₽ за день аренды
        return totalSum
    }
}