package com.college.culinaryexchange.data.local.dao

import androidx.room.*
import com.college.culinaryexchange.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUser(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostById(postId: String): Flow<PostEntity?>

    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    fun getPostCountByUser(userId: String): Flow<Int>

    @Query("SELECT * FROM posts WHERE category = :category ORDER BY timestamp DESC")
    fun getPostsByCategory(category: String): Flow<List<PostEntity>>

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Query("SELECT MAX(timestamp) FROM posts")
    suspend fun getLatestTimestamp(): Long?

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deleteById(postId: String)

    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    @Query("DELETE FROM posts")
    suspend fun deleteAll()
}
