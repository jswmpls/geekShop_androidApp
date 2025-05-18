package com.example.geekshop.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geekshop.repository.CartRepository
import com.example.geekshop.viewmodel.CartViewModel

class CartViewModelFactory(
    private val repository: CartRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

