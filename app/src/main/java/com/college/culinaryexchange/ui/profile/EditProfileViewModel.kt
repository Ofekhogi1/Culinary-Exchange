package com.college.culinaryexchange.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.repository.AuthRepository
import com.college.culinaryexchange.model.User
import kotlinx.coroutines.launch

class EditProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AuthRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun loadUser() {
        val uid = repository.currentUserId ?: return
        _isLoading.value = true
        viewModelScope.launch {
            _user.postValue(repository.getUser(uid))
            _isLoading.postValue(false)
        }
    }

    fun saveUser(name: String, bio: String, avatarUri: Uri?) {
        val uid = repository.currentUserId ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val existingUrl = _user.value?.avatarUrl.orEmpty()
            val avatarUrl = if (avatarUri != null) {
                val upload = runCatching { repository.uploadAvatar(getApplication(), avatarUri) }
                if (upload.isFailure) {
                    _operationResult.postValue(Result.failure(upload.exceptionOrNull()!!))
                    _isLoading.postValue(false)
                    return@launch
                }
                upload.getOrThrow()
            } else existingUrl
            val current = _user.value ?: User(id = uid)
            val updated = current.copy(name = name.trim(), bio = bio.trim(), avatarUrl = avatarUrl)
            val result = repository.updateUser(updated)
            _user.postValue(updated)
            _operationResult.postValue(result)
            _isLoading.postValue(false)
        }
    }
}
