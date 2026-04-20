package com.college.culinaryexchange.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.login(email, password)
                .onSuccess { _authState.postValue(AuthState.Success) }
                .onFailure { _authState.postValue(AuthState.Error(it.message ?: "Login failed")) }
        }
    }

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.register(name, email, password)
                .onSuccess { _authState.postValue(AuthState.Success) }
                .onFailure { _authState.postValue(AuthState.Error(it.message ?: "Registration failed")) }
        }
    }
}
