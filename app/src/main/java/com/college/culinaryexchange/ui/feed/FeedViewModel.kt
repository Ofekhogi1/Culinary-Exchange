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

data class CulinaryQuote(val text: String, val author: String)

enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao())

    companion object {
        private const val MAX_MEAL_RETRIES = 2

        val CULINARY_QUOTES = listOf(
            CulinaryQuote("Cooking is an art, but all art requires knowing something about the techniques and materials.", "Nathan Myhrvold"),
            CulinaryQuote("First we eat, then we do everything else.", "M.F.K. Fisher"),
            CulinaryQuote("The secret ingredient is always love.", "Unknown"),
            CulinaryQuote("People who love to eat are always the best people.", "Julia Child"),
            CulinaryQuote("A recipe has no soul. You, as the cook, must bring soul to the recipe.", "Thomas Keller"),
            CulinaryQuote("Food is our common ground, a universal experience.", "James Beard"),
            CulinaryQuote("Life is uncertain. Eat dessert first.", "Ernestine Ulmer")
        )
    }

    private var quoteIndex = 0
    private val _currentQuote = MutableLiveData(CULINARY_QUOTES[quoteIndex])
    val currentQuote: LiveData<CulinaryQuote> = _currentQuote

    fun refreshQuote() {
        quoteIndex = (quoteIndex + 1) % CULINARY_QUOTES.size
        _currentQuote.value = CULINARY_QUOTES[quoteIndex]
    }

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
            val result = if (sort == SortOrder.NEWEST_FIRST) searched.sortedByDescending { it.timestamp }
                         else searched.sortedBy { it.timestamp }
            android.util.Log.d(
                "FeedViewModel",
                "filteredPosts: total=${posts.size} category=$category query='$query' result=${result.size}"
            )
            value = result
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

    fun loadMealInspiration(retryCount: Int = 0) {
        _isMealLoading.value = true
        viewModelScope.launch {
            try {
                val meal = RetrofitClient.mealApi.getRandomMeal().meals?.firstOrNull()
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
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "loadMealInspiration failed (attempt ${retryCount + 1}): ${e.message}")
                if (retryCount < MAX_MEAL_RETRIES) {
                    android.util.Log.d("FeedViewModel", "Retrying loadMealInspiration…")
                    loadMealInspiration(retryCount + 1)
                    return@launch
                }
            } finally {
                _isMealLoading.postValue(false)
            }
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
            try {
                repository.refreshAllPosts()
                android.util.Log.d("FeedViewModel", "loadPosts succeeded")
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "loadPosts failed: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun retryLoad() = loadPosts()
}
