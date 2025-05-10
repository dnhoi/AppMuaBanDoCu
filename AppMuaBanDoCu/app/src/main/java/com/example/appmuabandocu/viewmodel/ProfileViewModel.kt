package com.example.appmuabandocu.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.data.uploadImageToCloudinary
import com.example.appmuabandocu.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")
    private val authRepository = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _avatarUrl = MutableStateFlow(auth.currentUser?.photoUrl?.toString() ?: "")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private var userSnapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        userSnapshotListener?.remove()
        _isLoading.value = true
        userSnapshotListener = userCollection.document(userId).addSnapshotListener { snapshot, error ->
            _isLoading.value = false
            if (error != null) {
                Log.e("ProfileViewModel", "Error listening to user data", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val emailStr = data["email"] as? String ?: ""
                    val isAdminVal = (data["isAdmin"] as? Boolean)
                        ?: (emailStr.startsWith("admin@") || emailStr == "admin@gmail.com")
                    val isBannedVal = (data["isBanned"] as? Boolean) ?: false
                    val banReasonVal = data["banReason"] as? String ?: ""
                    val user = User(
                        uid = data["id"] as? String ?: userId,
                        email = emailStr,
                        fullName = data["name"] as? String ?: "",
                        avatarUrl = data["avatarUrl"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String,
                        address = data["address"] as? String,
                        province = data["province"] as? String,
                        district = data["district"] as? String,
                        ward = data["ward"] as? String,
                        isAdmin = isAdminVal,
                        isBanned = isBannedVal,
                        banReason = banReasonVal
                    )
                    _userData.value = user
                    _avatarUrl.value = user.avatarUrl
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSnapshotListener?.remove()
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Upload image to Cloudinary
                val imageUrl = uploadImageToCloudinary(context, imageUri)

                if (imageUrl != null) {
                    // Update avatar URL in Firebase Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(imageUrl.toUri())
                        .build()

                    auth.currentUser?.updateProfile(profileUpdates)?.await()

                    // Update avatar URL in Firestore
                    userCollection.document(userId)
                        .update("avatarUrl", imageUrl)
                        .await()

                    // Update local state
                    _avatarUrl.value = imageUrl
                    _userData.value = _userData.value?.copy(avatarUrl = imageUrl)
                    _message.value = "Cập nhật ảnh đại diện thành công"
                } else {
                    _message.value = "Không thể tải lên ảnh đại diện"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading profile image", e)
                _message.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearMessage() {
        _message.value = null
    }

    fun updateUserProfile(user: User) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Update display name in Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.fullName)
                    .build()

                currentUser.updateProfile(profileUpdates).await()

                // Create map of fields to update in Firestore
                val updates = mapOf(
                    "name" to user.fullName,
                    "phoneNumber" to user.phoneNumber,
                    "address" to user.address,
                    "province" to user.province,
                    "district" to user.district,
                    "ward" to user.ward
                )

                // Update in Firestore
                userCollection.document(userId)
                    .update(updates)
                    .await()

                // Update local state
                _userData.value = user
                _message.value = "Cập nhật thông tin thành công"

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile", e)
                _message.value = "Lỗi khi cập nhật: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}