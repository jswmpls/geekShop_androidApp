package com.example.geekshop.repository

import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Category
import com.example.geekshop.data.model.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainRepository(private val sqlite: SQLite) {

    suspend fun getAllProducts(): List<Products> = withContext(Dispatchers.IO) {
        sqlite.getAllProducts()
    }

    suspend fun getInitialProducts(): List<Products> = withContext(Dispatchers.IO) {
        sqlite.getInitialProducts()
    }

    suspend fun addToCart(userId: Int, productId: Int): Boolean = withContext(Dispatchers.IO) {
        sqlite.addToCart(userId, productId)
    }

    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
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