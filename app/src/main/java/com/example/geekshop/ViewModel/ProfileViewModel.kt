// File: ProfileViewModel.kt
package com.example.geekshop.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.geekshop.repository.ProfileRepository

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _userBonus = MutableLiveData<Int>()
    val userBonus: LiveData<Int> get() = _userBonus

    fun loadUserBonus(userId: Int) {
        viewModelScope.launch {
            val bonus = profileRepository.getUserBonus(userId)
            _userBonus.postValue(bonus)
        }
    }

    // Для создания ViewModel
    class Factory(private val profileRepository: ProfileRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(profileRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}