package com.college.culinaryexchange.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.college.culinaryexchange.data.local.dao.PostDao
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class PostRepository(private val postDao: PostDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")

    fun getAllPostsFromCache(): Flow<List<PostEntity>> = postDao.getAllPosts()

    fun getUserPostsFromCache(userId: String): Flow<List<PostEntity>> =
        postDao.getPostsByUser(userId)

    fun getPostById(id: String): Flow<PostEntity?> = postDao.getPostById(id)

    fun getUserPostCount(userId: String): Flow<Int> = postDao.getPostCountByUser(userId)

    suspend fun refreshAllPosts() {
        Log.d(TAG, "refreshAllPosts: starting")
        try {
            val snapshot = postsCollection
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            Log.d(TAG, "refreshAllPosts: got ${snapshot.size()} docs from Firestore")
            val posts = snapshot.toObjects(Post::class.java)
            Log.d(TAG, "refreshAllPosts: mapped to ${posts.size} Post objects")
            postDao.upsertAll(posts.map { it.toEntity() })
            Log.d(TAG, "refreshAllPosts: upserted into Room OK")
        } catch (e: Exception) {
            Log.e(TAG, "refreshAllPosts: FAILED: ${e::class.simpleName}: ${e.message}", e)
            throw e
        }
    }

    suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit> = runCatching {
        val imageUrl = imageUri?.let { uploadImage(it) } ?: ""
        val finalPost = post.copy(id = UUID.randomUUID().toString(), imageUrl = imageUrl)
        postsCollection.document(finalPost.id).set(finalPost).await()
        postDao.upsert(finalPost.toEntity())
    }

    suspend fun updatePost(post: Post, imageUri: Uri?): Result<Unit> = runCatching {
        val imageUrl = if (imageUri != null) uploadImage(imageUri) else post.imageUrl
        val updated = post.copy(imageUrl = imageUrl)
        postsCollection.document(post.id).set(updated).await()
        postDao.upsert(updated.toEntity())
    }

    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        postsCollection.document(postId).delete().await()
        postDao.deleteById(postId)
    }

    private suspend fun uploadImage(uri: Uri): String = withContext(Dispatchers.IO) {
        val ctx = com.google.firebase.FirebaseApp.getInstance().applicationContext
        val original = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalStateException("Cannot read image from gallery")

        val scaled = scaleBitmap(original, MAX_IMAGE_PX)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
        val bytes = out.toByteArray()
        Log.d(TAG, "uploadImage: compressed to ${bytes.size} bytes")

        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        private const val TAG = "PostRepository"
        private const val MAX_IMAGE_PX = 800
        private const val IMAGE_QUALITY = 60

        fun scaleBitmap(bitmap: Bitmap, maxPx: Int): Bitmap {
            val w = bitmap.width
            val h = bitmap.height
            if (w <= maxPx && h <= maxPx) return bitmap
            val ratio = minOf(maxPx.toFloat() / w, maxPx.toFloat() / h)
            return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
        }
    }

    private fun Post.toEntity() = PostEntity(
        id = id, userId = userId, userName = userName,
        userAvatarUrl = userAvatarUrl, title = title,
        description = description, imageUrl = imageUrl, timestamp = timestamp,
        ingredients = ingredients, instructions = instructions,
        prepTime = prepTime, servings = servings, category = category,
        nutritionCalories = nutritionCalories, nutritionProtein = nutritionProtein,
        nutritionCarbs = nutritionCarbs, nutritionFat = nutritionFat
    )
}
