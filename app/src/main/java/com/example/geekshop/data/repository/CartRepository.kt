package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(private val sqlite: SQLite) {

    // Получение товаров в корзине
    fun getCartProducts(userId: Int): List<Pair<Products, Int>> {
        return sqlite.getCartProducts(userId)
    }

    // Удаление товара из корзины
    suspend fun removeFromCart(userId: Int, productId: Int) = withContext(Dispatchers.IO) {
        sqlite.removeFromCart(userId, productId)
    }

    // Очистка корзины пользователя
    suspend fun clearCart(userId: Int) = withContext(Dispatchers.IO) {
        sqlite.clearCart(userId)
    }

    // Получение количества бонусов пользователя
    fun getUserBonus(userId: Int): Int {
        return sqlite.getUserBonus(userId)
    }

    // Сохранение использованных бонусов
    suspend fun updateUserBonus(userId: Int, newBonus: Int) = withContext(Dispatchers.IO) {
        sqlite.updateUserBonus(userId, newBonus)
    }

    // Расчёт общей суммы заказа
    fun calculateTotalSum(cartItems: List<Pair<Products, Int>>, days: Int): Int {
        var totalSum = cartItems.sumOf { (product, quantity) ->
            product.Cost.replace(" ₽", "").toIntOrNull() ?: 0 * quantity
        }
        totalSum += days * 200 // 200 ₽ за день аренды
        return totalSum
    }
}