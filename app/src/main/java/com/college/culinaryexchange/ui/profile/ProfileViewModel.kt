package com.college.culinaryexchange.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.repository.AuthRepository
import com.college.culinaryexchange.model.User
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadUser() {
        val uid = repository.currentUserId ?: return
        _isLoading.value = true
        viewModelScope.launch {
            _user.postValue(repository.getUser(uid))
            _isLoading.postValue(false)
        }
    }

    fun logout() = repository.logout()
}
