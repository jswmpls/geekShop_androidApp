package com.example.geekshop.viewmodel

import androidx.lifecycle.*
import com.example.geekshop.repository.PayPalRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для обработки платежей через PayPal
 */
class PayPalViewModel(private val payPalRepository: PayPalRepository) : ViewModel() {

    // LiveData для передачи результата обновления бонусов
    private val _bonusUpdateResult = MutableLiveData<Pair<Int, Int>>()
    val bonusUpdateResult: LiveData<Pair<Int, Int>> = _bonusUpdateResult

    // LiveData для передачи статуса обработки платежа
    private val _paymentProcessed = MutableLiveData<Boolean>()
    val paymentProcessed: LiveData<Boolean> = _paymentProcessed

    /**
     * Обрабатывает платеж: обновляет бонусы и очищает корзину
     * @param userId ID пользователя
     * @param purchaseAmount Сумма покупки
     * @param usedBonus Использованные бонусы
     */
    fun processPayment(userId: Int, purchaseAmount: Int, usedBonus: Int) {
        viewModelScope.launch {
            try {
                val currentBonus = payPalRepository.getUserBonus(userId)
                val (finalBonus, bonusToAdd) = payPalRepository.calculateBonuses(
                    currentBonus,
                    usedBonus,
                    purchaseAmount
                )

                if (payPalRepository.updateUserBonus(userId, finalBonus)) {
                    payPalRepository.clearCart(userId)
                    _bonusUpdateResult.postValue(Pair(finalBonus, bonusToAdd))
                    _paymentProcessed.postValue(true)
                } else {
                    _paymentProcessed.postValue(false)
                }
            } catch (e: Exception) {
                _paymentProcessed.postValue(false)
            }
        }
    }

    companion object {
        /**
         * Фабрика для создания ViewModel с зависимостями
         */
        fun provideFactory(repository: PayPalRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PayPalViewModel(repository) as T
                }
            }
        }
    }
}