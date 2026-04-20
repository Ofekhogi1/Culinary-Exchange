package com.college.culinaryexchange.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val timestamp: Long
)
