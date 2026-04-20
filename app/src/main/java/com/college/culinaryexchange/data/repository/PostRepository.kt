package com.college.culinaryexchange.data.repository

import android.net.Uri
import com.college.culinaryexchange.data.local.dao.PostDao
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostRepository(private val postDao: PostDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val postsCollection = firestore.collection("posts")

    fun getAllPostsFromCache(): Flow<List<PostEntity>> = postDao.getAllPosts()

    fun getUserPostsFromCache(userId: String): Flow<List<PostEntity>> =
        postDao.getPostsByUser(userId)

    suspend fun refreshAllPosts() {
        val snapshot = postsCollection.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
        val posts = snapshot.toObjects(Post::class.java)
        postDao.upsertAll(posts.map { it.toEntity() })
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

    private suspend fun uploadImage(uri: Uri): String {
        val ref = storage.reference.child("posts/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun Post.toEntity() = PostEntity(
        id = id, userId = userId, userName = userName,
        userAvatarUrl = userAvatarUrl, title = title,
        description = description, imageUrl = imageUrl, timestamp = timestamp
    )
}
