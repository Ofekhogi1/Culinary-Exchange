package com.college.culinaryexchange.ui.post

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.local.AppDatabase
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.data.repository.PostRepository
import kotlinx.coroutines.launch

class RecipeDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao())

    private val _post = MutableLiveData<PostEntity?>()
    val post: LiveData<PostEntity?> = _post

    private val _notFound = MutableLiveData(false)
    val notFound: LiveData<Boolean> = _notFound

    fun loadPost(postId: String) {
        if (postId.isBlank()) {
            _notFound.value = true
            return
        }
        viewModelScope.launch {
            repository.getPostById(postId).collect { entity ->
                if (entity == null) _notFound.postValue(true)
                else _post.postValue(entity)
            }
        }
    }
}
