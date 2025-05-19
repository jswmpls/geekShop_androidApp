package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Category
import com.example.geekshop.data.model.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с данными главного экрана
 */
class MainRepository(private val sqlite: SQLite) {

    /**
     * Получение всех товаров из базы данных
     */
    suspend fun getAllProducts(): List<Products> = withContext(Dispatchers.IO) {
        sqlite.getAllProducts()
    }

    /**
     * Получение начального набора товаров (если база пуста)
     */
    suspend fun getInitialProducts(): List<Products> = withContext(Dispatchers.IO) {
        sqlite.getInitialProducts()
    }

    /**
     * Добавление товара в корзину пользователя
     */
    suspend fun addToCart(userId: Int, productId: Int): Boolean = withContext(Dispatchers.IO) {
        sqlite.addToCart(userId, productId)
    }

    /**
     * Получение списка всех категорий
     */
    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        // Захардкоженный список категорий
        listOf(
            Category("All"),
            Category("AAA-игры"),
            Category("VR"),
            Category("Free-To-Play"),
            Category("Эксклюзивы"),
            Category("Инди-игры"),
            Category("Классика")
        )
    }
}