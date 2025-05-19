package com.example.geekshop.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.geekshop.data.model.Products
import com.example.geekshop.repository.CartRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для управления данными корзины
 */
class CartViewModel(private val cartRepository: CartRepository, private val appContext: Context) : ViewModel() {

    // LiveData для товаров в корзине
    private val _cartItems = MutableLiveData<List<Pair<Products, Int>>>()
    val cartItems: LiveData<List<Pair<Products, Int>>> get() = _cartItems

    // LiveData для общей суммы заказа
    private val _totalSum = MutableLiveData<Int>()
    val totalSum: LiveData<Int> get() = _totalSum

    // LiveData для состояния применения бонусов
    private val _isBonusApplied = MutableLiveData<Boolean>()
    val isBonusApplied: LiveData<Boolean> get() = _isBonusApplied

    // LiveData для сообщений Toast
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    // Первоначальная сумма заказа
    private var originalTotalSum = 0

    init {
        loadCartItems()
    }

    // Загрузка товаров из корзины
    fun loadCartItems() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val items = cartRepository.getCartProducts(userId)
            _cartItems.value = items
            updateTotalSum(1)
            originalTotalSum = _totalSum.value ?: 0
        }
    }

    // Удаление товара из корзины
    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            cartRepository.removeFromCart(getCurrentUserId(), productId)
            loadCartItems()
        }
    }

    // Применение бонусов
    fun applyBonuses() {
        viewModelScope.launch {
            val userBonus = cartRepository.getUserBonus(getCurrentUserId())
            val currentTotal = _totalSum.value ?: 0
            if (userBonus > 0 && currentTotal > 0) {
                val bonusAmount = kotlin.math.min(userBonus, currentTotal)
                val newTotalSum = kotlin.math.max(0, currentTotal - bonusAmount) // Не может быть меньше 0
                _totalSum.postValue(newTotalSum)
                _isBonusApplied.postValue(true)
                saveUsedBonus(bonusAmount)
                _toastMessage.postValue("Будет списано $bonusAmount бонусов")
            } else {
                _isBonusApplied.postValue(false)
                _toastMessage.postValue(
                    if (currentTotal <= 0) "Сумма покупки уже нулевая"
                    else "Недостаточно бонусов. Доступно: $userBonus"
                )
            }
        }
    }

    // Сброс использования бонусов
    fun resetBonuses() {
        _totalSum.value = originalTotalSum
        _isBonusApplied.value = false
        clearUsedBonus()
        _toastMessage.value = "Бонусы не будут использованы"
    }

    // Обновление общей суммы заказа
    fun updateTotalSum(days: Int) {
        val cartItems = _cartItems.value ?: emptyList()
        val totalSum = cartRepository.calculateTotalSum(cartItems, days)
        _totalSum.value = totalSum
        // Сохраняем исходную сумму при первом расчете
        if (originalTotalSum == 0) {
            originalTotalSum = totalSum
        }
    }

    // Сохранение суммы покупки
    fun savePurchaseAmount() {
        val total = totalSum.value ?: 0
        val userId = getCurrentUserId()
        if (userId != -1) {
            viewModelScope.launch {
                // Сохраняем сумму покупки в SharedPreferences
                val sharedPref = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putInt("purchase_amount", total)
                    apply()
                }
                // Очищаем корзину после покупки
                cartRepository.clearCart(userId)
            }
        }
    }

    // Получение текущего ID пользователя
    private fun getCurrentUserId(): Int {
        return appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("current_user_id", -1)
    }

    // Сохранение использованных бонусов
    private fun saveUsedBonus(amount: Int) {
        val sharedPref = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("used_bonus", amount).apply()
    }

    // Очистка использованных бонусов
    private fun clearUsedBonus() {
        val sharedPref = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().remove("used_bonus").apply()
    }

    companion object {
        const val ORIGINAL_TOTAL_SUM = "original_total_sum"
    }
}