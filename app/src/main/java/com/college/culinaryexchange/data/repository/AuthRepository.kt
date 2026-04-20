package com.college.culinaryexchange.data.repository

import com.college.culinaryexchange.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw IllegalStateException("Auth succeeded but user is null")
        val user = User(id = uid, name = name, email = email)
        runCatching { db.collection("users").document(uid).set(user).await() }
            .onFailure {
                auth.currentUser?.delete()?.await()
                throw it
            }
    }

    suspend fun getUser(uid: String): User? =
        db.collection("users").document(uid).get().await().toObject(User::class.java)

    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        db.collection("users").document(user.id).set(user).await()
    }

    fun logout() = auth.signOut()
}
