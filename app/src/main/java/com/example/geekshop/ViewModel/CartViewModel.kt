package com.example.geekshop.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.geekshop.data.model.Products
import com.example.geekshop.repository.CartRepository
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository, private val appContext: Context) : ViewModel() {

    private val _cartItems = MutableLiveData<List<Pair<Products, Int>>>()
    val cartItems: LiveData<List<Pair<Products, Int>>> = _cartItems

    private val _totalSum = MutableLiveData<Int>()
    val totalSum: LiveData<Int> get() = _totalSum

    private val _isBonusApplied = MutableLiveData<Boolean>()
    val isBonusApplied: LiveData<Boolean> get() = _isBonusApplied

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private var originalTotalSum = 0

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val items = cartRepository.getCartProducts(userId)
            _cartItems.value = items
            updateTotalSum(1)
            originalTotalSum = _totalSum.value ?: 0
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            cartRepository.removeFromCart(getUserId(), productId)
            loadCartItems()
        }
    }

    fun applyBonuses() {
        viewModelScope.launch {
            val userBonus = cartRepository.getUserBonus(getUserId())
            val currentTotal = _totalSum.value ?: 0

            if (userBonus > 0 && currentTotal > 0) {
                val bonusAmount = minOf(userBonus, currentTotal)
                val newTotalSum = maxOf(0, currentTotal - bonusAmount) // Не может быть меньше 0
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

    fun resetBonuses() {
        _totalSum.value = originalTotalSum
        _isBonusApplied.value = false
        clearUsedBonus()
        _toastMessage.value = "Бонусы не будут использованы"
    }

    fun updateTotalSum(days: Int) {
        val cartItems = _cartItems.value ?: emptyList()
        val totalSum = cartRepository.calculateTotalSum(cartItems, days)
        _totalSum.value = totalSum
        // Сохраняем исходную сумму при первом расчете
        if (originalTotalSum == 0) {
            originalTotalSum = totalSum
        }
    }

    fun savePurchaseAmount() {
        val total = totalSum.value ?: 0
        val userId = getUserId()
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

    private fun getCurrentUserId(): Int {
        return getUserId()
    }

    private fun getUserId(): Int {
        return appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getInt("current_user_id", -1)
    }

    private fun saveUsedBonus(amount: Int) {
        val sharedPref = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("used_bonus", amount).apply()
    }

    private fun clearUsedBonus() {
        val sharedPref = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().remove("used_bonus").apply()
    }

    companion object {
        const val ORIGINAL_TOTAL_SUM = "original_total_sum"
    }
}