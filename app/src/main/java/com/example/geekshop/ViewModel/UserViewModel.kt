package com.example.geekshop.viewmodel

import androidx.lifecycle.*
import com.example.geekshop.data.model.Users
import com.example.geekshop.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _registrationResult = MutableLiveData<Long>()
    val registrationResult: LiveData<Long> get() = _registrationResult

    private val _authenticationResult = MutableLiveData<Users?>()
    val authenticationResult: LiveData<Users?> get() = _authenticationResult

    private val _isLoginExists = MutableLiveData<Boolean>()
    val isLoginExists: LiveData<Boolean> get() = _isLoginExists

    // Регистрация нового пользователя
    fun registerUser(name: String, login: String, password: String) {
        viewModelScope.launch {
            val nextId = userRepository.getNextUserId()
            val user = Users(nextId, name, login, password, 0)
            val userId = userRepository.addUser(user)
            _registrationResult.postValue(userId)
        }
    }

    // Проверка существования логина
    fun checkLoginExists(login: String) {
        viewModelScope.launch {
            val exists = userRepository.isLoginExists(login)
            _isLoginExists.postValue(exists)
        }
    }

    // Авторизация пользователя
    fun authenticateUser(login: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.authenticateUser(login, password)
            _authenticationResult.postValue(user)
        }
    }
}