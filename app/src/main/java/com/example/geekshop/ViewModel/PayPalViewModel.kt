package com.example.geekshop.viewmodel

import androidx.lifecycle.*
import com.example.geekshop.repository.PayPalRepository
import kotlinx.coroutines.launch

class PayPalViewModel(private val payPalRepository: PayPalRepository) : ViewModel() {

    private val _bonusUpdateResult = MutableLiveData<Pair<Int, Int>>()
    val bonusUpdateResult: LiveData<Pair<Int, Int>> get() = _bonusUpdateResult

    private val _paymentProcessed = MutableLiveData<Boolean>()
    val paymentProcessed: LiveData<Boolean> get() = _paymentProcessed

    fun processPayment(userId: Int, purchaseAmount: Int, usedBonus: Int) {
        viewModelScope.launch {
            val currentBonus = payPalRepository.getUserBonus(userId)
            val (finalBonus, bonusToAdd) = payPalRepository.calculateBonuses(currentBonus, usedBonus, purchaseAmount)
            if (payPalRepository.updateUserBonus(userId, finalBonus)) {
                payPalRepository.clearCart(userId)
                _bonusUpdateResult.postValue(Pair(finalBonus, bonusToAdd))
                _paymentProcessed.postValue(true)
            } else {
                _paymentProcessed.postValue(false)
            }
        }
    }

    companion object {
        fun provideFactory(repository: PayPalRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PayPalViewModel::class.java)) {
                        return PayPalViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}