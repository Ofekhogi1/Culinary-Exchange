package com.college.culinaryexchange.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTime: Int = 0,
    val servings: Int = 0,
    val category: String = "",
    val nutritionCalories: Int = 0,
    val nutritionProtein: String = "",
    val nutritionCarbs: String = "",
    val nutritionFat: String = ""
)
