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
import com.college.culinaryexchange.data.repository.PostRepository
import kotlinx.coroutines.launch

data class Quote(val text: String, val author: String)

enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PostRepository(AppDatabase.getInstance(app).postDao(), app)

    private val _allPosts: LiveData<List<PostEntity>> = repository.getAllPostsFromCache().asLiveData()
    private val _sortOrder = MutableLiveData(SortOrder.NEWEST_FIRST)
    private val _selectedCategory = MutableLiveData("All")

    val filteredPosts: LiveData<List<PostEntity>> = MediatorLiveData<List<PostEntity>>().apply {
        fun update() {
            val posts = _allPosts.value ?: return
            val sort = _sortOrder.value ?: SortOrder.NEWEST_FIRST
            val category = _selectedCategory.value ?: "All"
            val filtered = if (category == "All") posts else posts.filter { it.category == category }
            value = if (sort == SortOrder.NEWEST_FIRST) filtered.sortedByDescending { it.timestamp }
                    else filtered.sortedBy { it.timestamp }
        }
        addSource(_allPosts) { update() }
        addSource(_sortOrder) { update() }
        addSource(_selectedCategory) { update() }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val quotes = listOf(
        Quote("Cooking is like love. It should be entered into with abandon or not at all.", "Harriet van Horne"),
        Quote("People who love to eat are always the best people.", "Julia Child"),
        Quote("The secret ingredient is always love.", "Unknown"),
        Quote("Food is our common ground, a universal experience.", "James Beard"),
        Quote("Cooking is at once child's play and adult joy.", "Craig Claiborne"),
        Quote("First we eat, then we do everything else.", "M.F.K. Fisher"),
        Quote("The discovery of a new dish does more for human happiness than the discovery of a new star.", "Jean Brillat-Savarin")
    )
    private var quoteIndex = (quotes.indices).random()

    private val _currentQuote = MutableLiveData(quotes[quoteIndex])
    val currentQuote: LiveData<Quote> = _currentQuote

    fun refreshQuote() {
        quoteIndex = (quoteIndex + 1) % quotes.size
        _currentQuote.value = quotes[quoteIndex]
    }

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setCategory(category: String) { _selectedCategory.value = category }
    fun resetFilters() {
        _sortOrder.value = SortOrder.NEWEST_FIRST
        _selectedCategory.value = "All"
    }

    fun loadPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching { repository.refreshAllPosts() }
            _isLoading.postValue(false)
        }
    }
}
