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

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deleteById(postId: String)

    @Query("DELETE FROM posts")
    suspend fun deleteAll()
}
