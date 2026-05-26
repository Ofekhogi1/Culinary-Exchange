package com.college.culinaryexchange.ui.post

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.local.AppDatabase
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.data.repository.AuthRepository
import com.college.culinaryexchange.data.repository.PostRepository
import com.college.culinaryexchange.model.Post
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PostViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao())
    private val authRepository = AuthRepository()

    private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid
        ?: run { Log.w("PostViewModel", "No authenticated user"); "" }

    val userPosts: LiveData<List<PostEntity>> =
        repository.getUserPostsFromCache(currentUserId).asLiveData()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _editPost = MutableLiveData<PostEntity?>()
    val editPost: LiveData<PostEntity?> = _editPost

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    fun updateFormValidity(
        title: String,
        description: String,
        ingredients: List<String>,
        instructions: List<String>
    ) {
        _isFormValid.value = title.isNotBlank() && description.isNotBlank()
            && ingredients.isNotEmpty() && instructions.isNotEmpty()
    }

    fun loadPostForEdit(postId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _editPost.postValue(
                repository.getUserPostsFromCache(currentUserId).first()
                    .find { it.id == postId }
            )
        }
    }

    fun loadUserPosts() {
        if (currentUserId.isBlank()) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.refreshUserPosts(currentUserId)
            } catch (e: Exception) {
                Log.e("PostViewModel", "loadUserPosts failed: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Creates a new post. The current user's display name is resolved via [AuthRepository]
     * so the Fragment never needs to touch Firebase directly.
     */
    fun createPost(
        title: String,
        description: String,
        imageUri: Uri?,
        ingredients: List<String> = emptyList(),
        instructions: List<String> = emptyList(),
        prepTime: Int = 0,
        servings: Int = 0,
        category: String = "",
        nutritionCalories: Int = 0,
        nutritionProtein: String = "",
        nutritionCarbs: String = "",
        nutritionFat: String = ""
    ) {
        if (currentUserId.isBlank()) return
        _isLoading.value = true
        viewModelScope.launch {
            val userName = runCatching { authRepository.getUser(currentUserId)?.name }
                .getOrNull() ?: "Anonymous"
            val post = Post(
                userId = currentUserId,
                userName = userName,
                title = title,
                description = description,
                timestamp = System.currentTimeMillis(),
                ingredients = ingredients,
                instructions = instructions,
                prepTime = prepTime,
                servings = servings,
                category = category,
                nutritionCalories = nutritionCalories,
                nutritionProtein = nutritionProtein,
                nutritionCarbs = nutritionCarbs,
                nutritionFat = nutritionFat
            )
            val result = repository.createPost(post, imageUri)
            _operationResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun updatePost(post: Post, imageUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updatePost(post, imageUri)
            _operationResult.postValue(result)
            _isLoading.postValue(false)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)
            _operationResult.postValue(result)
        }
    }
}
