package com.example.appmuabandocu.repository

import androidx.core.net.toUri
import com.example.appmuabandocu.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    // firestore auth
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")

    suspend fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun registerWithEmailPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun updateUserProfile(user: User) {
        userCollection.document(user.uid).set(user.toMap()).await()
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(user.fullName)
            .setPhotoUri(user.avatarUrl.toUri())
            .build()

        auth.currentUser?.updateProfile(profileUpdate)?.await()
    }

    suspend fun signWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun isUserRegistered(userId: String): Boolean {
        return try {
            val document = userCollection.document(userId).get().await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

}