package com.example.geekshop.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.geekshop.data.model.Category
import com.example.geekshop.data.model.Products
import com.example.geekshop.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository, private val context: Context) : ViewModel() {

    // LiveData для продуктов и категорий
    private val _products = MutableLiveData<List<Products>>()
    val products: LiveData<List<Products>> get() = _products

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _currentCategory = MutableLiveData<String>("All")
    val currentCategory: LiveData<String> get() = _currentCategory

    init {
        loadCategories()
        loadProducts()
    }

    // Загрузка всех товаров
    private fun loadProducts() {
        viewModelScope.launch {
            val allProducts = repository.getAllProducts()
            if (allProducts.isEmpty()) {
                val initialProducts = repository.getInitialProducts()
                _products.postValue(initialProducts)
            } else {
                _products.postValue(allProducts)
            }
        }
    }

    // Загрузка списка категорий
    private fun loadCategories() {
        viewModelScope.launch {
            val categoriesList = repository.getAllCategories()
            _categories.postValue(categoriesList)
        }
    }

    // Обновление товаров по категории
    fun updateProductsByCategory(category: String) {
        viewModelScope.launch {
            _currentCategory.postValue(category)

            val allProducts = repository.getAllProducts()
            val filteredProducts = if (category == "All") {
                allProducts
            } else {
                allProducts.filter { it.Category == category }
            }

            _products.postValue(filteredProducts)
        }
    }

    // Добавление товара в корзину
    fun addProductToCart(productId: Int) {
        val userId = getCurrentUserId()
        if (userId == -1) return

        viewModelScope.launch {
            val success = repository.addToCart(userId, productId)
            if (!success) {
                Log.e("CartError", "Failed to add product $productId for user $userId")
            }
        }
    }

    // Получение ID текущего пользователя
    fun getCurrentUserId(): Int {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("current_user_id", -1)
    }

    companion object {
        fun provideFactory(repository: MainRepository, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        return MainViewModel(repository, context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}