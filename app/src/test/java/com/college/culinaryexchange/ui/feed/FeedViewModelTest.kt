package com.college.culinaryexchange.ui.feed

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.college.culinaryexchange.data.local.AppDatabase
import com.college.culinaryexchange.data.local.dao.PostDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        // Mock Firebase statics so PostRepository constructor doesn't need Android process
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseStorage::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
        every { FirebaseStorage.getInstance() } returns mockk(relaxed = true)

        // Mock Room DB companion
        mockkObject(AppDatabase.Companion)
        val mockDao = mockk<PostDao>()
        val mockDb = mockk<AppDatabase>(relaxed = true)
        every { AppDatabase.getInstance(any()) } returns mockDb
        every { mockDb.postDao() } returns mockDao
        every { mockDao.getAllPosts() } returns flowOf(emptyList())
        every { mockDao.getPostsByUser(any()) } returns flowOf(emptyList())

        val app = mockk<Application>(relaxed = true)
        viewModel = FeedViewModel(app)
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun `currentQuote is never null on creation`() {
        assertNotNull(viewModel.currentQuote.value)
    }

    @Test
    fun `refreshQuote changes the current quote`() {
        val first = viewModel.currentQuote.value!!
        viewModel.refreshQuote()
        val second = viewModel.currentQuote.value!!
        assert(first.text != second.text) { "refreshQuote should move to a different quote" }
    }

    @Test
    fun `refreshQuote wraps around after full cycle`() {
        val initial = viewModel.currentQuote.value!!
        repeat(7) { viewModel.refreshQuote() }
        val afterFullCycle = viewModel.currentQuote.value!!
        assert(initial.text == afterFullCycle.text) {
            "After 7 refreshes on 7-item list, should return to initial quote"
        }
    }

    @Test
    fun `currentQuote text and author are non-blank`() {
        val quote = viewModel.currentQuote.value!!
        assert(quote.text.isNotBlank()) { "Quote text should not be blank" }
        assert(quote.author.isNotBlank()) { "Quote author should not be blank" }
    }
}
