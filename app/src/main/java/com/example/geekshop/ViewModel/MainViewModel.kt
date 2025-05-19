package com.example.geekshop.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.geekshop.data.model.Category
import com.example.geekshop.data.model.Products
import com.example.geekshop.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана приложения
 */
class MainViewModel(private val repository: MainRepository, private val context: Context) : ViewModel() {

    // LiveData для списка товаров
    private val _products = MutableLiveData<List<Products>>()
    val products: LiveData<List<Products>> get() = _products

    // LiveData для списка категорий
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    // LiveData для текущей выбранной категории
    private val _currentCategory = MutableLiveData<String>("All")
    val currentCategory: LiveData<String> get() = _currentCategory

    init {
        loadCategories()
        loadProducts()
    }

    /**
     * Загрузка всех товаров из репозитория
     */
    private fun loadProducts() {
        viewModelScope.launch {
            val allProducts = repository.getAllProducts()
            if (allProducts.isEmpty()) {
                // Если база пуста, загружаем начальный набор товаров
                val initialProducts = repository.getInitialProducts()
                _products.postValue(initialProducts)
            } else {
                _products.postValue(allProducts)
            }
        }
    }

    /**
     * Загрузка списка категорий
     */
    private fun loadCategories() {
        viewModelScope.launch {
            val categoriesList = repository.getAllCategories()
            _categories.postValue(categoriesList)
        }
    }

    /**
     * Фильтрация товаров по категории
     * @param category Название категории для фильтрации ("All" - все товары)
     */
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

    /**
     * Добавление товара в корзину пользователя
     * @param productId ID товара для добавления
     */
    fun addProductToCart(productId: Int) {
        val userId = getCurrentUserId()
        if (userId == -1) return

        viewModelScope.launch {
            repository.addToCart(userId, productId)
        }
    }

    /**
     * Получение ID текущего авторизованного пользователя
     */
    fun getCurrentUserId(): Int {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("current_user_id", -1)
    }

    companion object {
        /**
         * Фабрика для создания ViewModel с параметрами
         */
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