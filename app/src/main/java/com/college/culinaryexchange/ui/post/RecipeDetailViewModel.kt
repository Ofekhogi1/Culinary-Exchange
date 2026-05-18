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

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao(), app)

    private val _post = MutableLiveData<PostEntity?>()
    val post: LiveData<PostEntity?> = _post

    fun loadPost(postId: String) {
        viewModelScope.launch {
            repository.getPostById(postId).collect { _post.postValue(it) }
        }
    }
}
