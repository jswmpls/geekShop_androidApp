package com.example.geekshop.viewmodel

import androidx.lifecycle.*
import com.example.geekshop.data.model.Users
import com.example.geekshop.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Long>()
    val registrationResult: LiveData<Long> = _registrationResult

    private val _authenticationResult = MutableLiveData<Users?>()
    val authenticationResult: LiveData<Users?> = _authenticationResult

    private val _isLoginExists = MutableLiveData<Boolean>()
    val isLoginExists: LiveData<Boolean> = _isLoginExists

    private val _nextUserId = MutableLiveData<Int>()
    val nextUserId: LiveData<Int> = _nextUserId

    fun registerUser(name: String, login: String, password: String) {
        viewModelScope.launch {
            val nextId = userRepository.getNextUserId()
            val user = Users(nextId, name, login, password, 0)
            val userId = userRepository.addUser(user)
            _registrationResult.postValue(userId)
        }
    }

    fun checkLoginExists(login: String) {
        viewModelScope.launch {
            val exists = userRepository.isLoginExists(login)
            _isLoginExists.postValue(exists)
        }
    }

    fun getNextUserId() {
        viewModelScope.launch {
            val id = userRepository.getNextUserId()
            _nextUserId.postValue(id)
        }
    }

    fun authenticateUser(login: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.authenticateUser(login, password)
            _authenticationResult.postValue(user)
        }
    }
}