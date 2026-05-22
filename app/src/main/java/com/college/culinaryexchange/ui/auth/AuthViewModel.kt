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
    object PasswordResetSent : AuthState()
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
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("no user record", ignoreCase = true) == true ->
                            "No account found for this email"
                        e.message?.contains("password is invalid", ignoreCase = true) == true ->
                            "Incorrect password — please try again"
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Network error — check your connection"
                        else -> e.message ?: "Login failed"
                    }
                    _authState.postValue(AuthState.Error(msg))
                }
        }
    }

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.register(name, email, password)
                .onSuccess { _authState.postValue(AuthState.Success) }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                            "This email is already registered — try logging in"
                        e.message?.contains("network", ignoreCase = true) == true ->
                            "Network error — check your connection"
                        else -> e.message ?: "Registration failed"
                    }
                    _authState.postValue(AuthState.Error(msg))
                }
        }
    }

    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            repository.resetPassword(email)
                .onSuccess { _authState.postValue(AuthState.PasswordResetSent) }
                .onFailure { _authState.postValue(AuthState.Error(it.message ?: "Could not send reset email")) }
        }
    }
}
