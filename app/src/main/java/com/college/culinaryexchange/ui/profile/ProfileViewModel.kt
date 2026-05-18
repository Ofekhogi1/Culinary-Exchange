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

    val recipeCount: LiveData<Int> = postRepository.getUserPostCount(currentUserId).asLiveData()

    val userPosts: LiveData<List<PostEntity>> =
        postRepository.getUserPostsFromCache(currentUserId).asLiveData()

    fun deletePost(postId: String) {
        viewModelScope.launch { postRepository.deletePost(postId) }
    }

    fun loadUser() {
        val uid = authRepository.currentUserId ?: return
        _isLoading.value = true
        viewModelScope.launch {
            _user.postValue(authRepository.getUser(uid))
            _isLoading.postValue(false)
        }
    }

    fun logout() = authRepository.logout()
}
