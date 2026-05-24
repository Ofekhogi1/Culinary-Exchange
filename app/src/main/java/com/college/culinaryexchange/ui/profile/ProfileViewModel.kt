package com.college.culinaryexchange.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.local.AppDatabase
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.data.repository.AuthRepository
import com.college.culinaryexchange.data.repository.PostRepository
import com.college.culinaryexchange.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepository = AuthRepository()
    private val postRepository = PostRepository(AppDatabase.getInstance(app).postDao())
    private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    val recipeCount: LiveData<Int> = postRepository.getUserPostCount(currentUserId).asLiveData()

    val userPosts: LiveData<List<PostEntity>> =
        postRepository.getUserPostsFromCache(currentUserId).asLiveData()

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            _operationResult.postValue(result)
        }
    }

    fun loadUser() {
        val uid = authRepository.currentUserId ?: return
        if (_isLoading.value == true) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authRepository.getUser(uid)
                _user.postValue(user)
                if (user == null) _error.postValue("Could not load profile — check your connection")
                runCatching { postRepository.refreshAllPosts() }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "loadUser failed: ${e.message}", e)
                _error.postValue(e.message ?: "Unexpected error loading profile")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshUser() {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            runCatching { authRepository.getUser(uid) }
                .onSuccess { _user.postValue(it) }
                .onFailure { _error.postValue("Refresh failed: ${it.message}") }
        }
    }

    fun logout() = authRepository.logout()
}
