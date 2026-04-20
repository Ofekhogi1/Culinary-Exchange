package com.college.culinaryexchange.data.remote.api

import retrofit2.http.GET

data class MealResponse(val meals: List<Meal>?)
data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String
)

interface MealApiService {
    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse
}
