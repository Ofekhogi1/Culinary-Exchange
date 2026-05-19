package com.college.culinaryexchange.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.college.culinaryexchange.data.repository.PostRepository.Companion.scaleBitmap
import com.college.culinaryexchange.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
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

    suspend fun uploadAvatar(uri: Uri): String = withContext(Dispatchers.IO) {
        val ctx = com.google.firebase.FirebaseApp.getInstance().applicationContext
        val original = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalStateException("Cannot read avatar from gallery")

        val scaled = scaleBitmap(original, 400)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)

        "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun logout() = auth.signOut()
}
