package com.college.culinaryexchange.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.college.culinaryexchange.data.local.AppDatabase
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.data.remote.api.RetrofitClient
import com.college.culinaryexchange.data.repository.PostRepository
import kotlinx.coroutines.launch

data class MealInspiration(
    val name: String,
    val category: String,
    val area: String,
    val thumbnailUrl: String
)

enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao())

    private val _allPosts: LiveData<List<PostEntity>> = repository.getAllPostsFromCache().asLiveData()
    private val _sortOrder = MutableLiveData(SortOrder.NEWEST_FIRST)
    private val _selectedCategory = MutableLiveData("All")
    private val _searchQuery = MutableLiveData("")

    val searchQuery: LiveData<String> = _searchQuery

    val filteredPosts: LiveData<List<PostEntity>> = MediatorLiveData<List<PostEntity>>().apply {
        fun update() {
            val posts = _allPosts.value ?: return
            val sort = _sortOrder.value ?: SortOrder.NEWEST_FIRST
            val category = _selectedCategory.value ?: "All"
            val query = _searchQuery.value.orEmpty().trim()
            val filtered = if (category == "All") posts else posts.filter { it.category == category }
            val searched = if (query.isBlank()) filtered
                           else filtered.filter { it.title.contains(query, ignoreCase = true) }
            value = if (sort == SortOrder.NEWEST_FIRST) searched.sortedByDescending { it.timestamp }
                    else searched.sortedBy { it.timestamp }
        }
        addSource(_allPosts) { update() }
        addSource(_sortOrder) { update() }
        addSource(_selectedCategory) { update() }
        addSource(_searchQuery) { update() }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // --- Meal Inspiration from TheMealDB REST API ---
    private val _mealInspiration = MutableLiveData<MealInspiration?>()
    val mealInspiration: LiveData<MealInspiration?> = _mealInspiration

    private val _isMealLoading = MutableLiveData(false)
    val isMealLoading: LiveData<Boolean> = _isMealLoading

    fun loadMealInspiration() {
        _isMealLoading.value = true
        viewModelScope.launch {
            runCatching { RetrofitClient.mealApi.getRandomMeal().meals?.firstOrNull() }
                .onSuccess { meal ->
                    _mealInspiration.postValue(
                        meal?.let {
                            MealInspiration(
                                name = it.strMeal,
                                category = it.strCategory,
                                area = it.strArea,
                                thumbnailUrl = it.strMealThumb
                            )
                        }
                    )
                }
                .onFailure {
                    android.util.Log.e("FeedViewModel", "loadMealInspiration failed: ${it.message}")
                }
            _isMealLoading.postValue(false)
        }
    }

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setCategory(category: String) { _selectedCategory.value = category }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun clearSearch() { _searchQuery.value = "" }
    fun resetFilters() {
        _sortOrder.value = SortOrder.NEWEST_FIRST
        _selectedCategory.value = "All"
        _searchQuery.value = ""
    }

    fun loadPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching { repository.refreshAllPosts() }
                .onFailure { android.util.Log.e("FeedViewModel", "loadPosts failed: ${it.message}", it) }
                .onSuccess { android.util.Log.d("FeedViewModel", "loadPosts succeeded") }
            _isLoading.postValue(false)
        }
    }
}
