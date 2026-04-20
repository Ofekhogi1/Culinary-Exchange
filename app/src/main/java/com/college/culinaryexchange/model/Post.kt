package com.college.culinaryexchange.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)
