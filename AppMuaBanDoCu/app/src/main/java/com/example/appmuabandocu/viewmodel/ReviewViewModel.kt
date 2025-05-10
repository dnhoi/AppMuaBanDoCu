package com.example.appmuabandocu.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReviewViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val reviewsRef = db.getReference("reviews")

    private val _reviewList = mutableStateListOf<Review>()
    val reviewList: List<Review> get() = _reviewList

    val averageRating = mutableStateOf(5.0f)
    val totalReviews = mutableStateOf(0)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun loadReviewsForSeller(sellerId: String) {
        if (sellerId.isEmpty()) return
        _isLoading.value = true

        reviewsRef.child(sellerId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Review>()
                snapshot.children.forEach { child ->
                    val r = child.getValue(Review::class.java)
                    r?.let { list.add(it) }
                }
                _reviewList.clear()
                _reviewList.addAll(list.sortedByDescending { it.timestamp })
                totalReviews.value = list.size
                averageRating.value = if (list.isNotEmpty()) {
                    list.map { it.rating }.average().toFloat()
                } else 5.0f
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                Log.e("ReviewViewModel", "Error loading reviews: ${error.message}")
            }
        })
    }

    fun submitReview(sellerId: String, rating: Int, comment: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "Người mua ẩn danh"
        val currentUserAvatar = auth.currentUser?.photoUrl?.toString() ?: ""

        val newRef = reviewsRef.child(sellerId).push()
        val reviewId = newRef.key ?: return

        val review = Review(
            id = reviewId,
            sellerId = sellerId,
            reviewerId = currentUserId,
            reviewerName = currentUserName,
            reviewerAvatar = currentUserAvatar,
            rating = rating,
            comment = comment.trim(),
            timestamp = Date().time
        )

        viewModelScope.launch {
            try {
                _isLoading.value = true
                newRef.setValue(review).await()
                _message.value = "Cảm ơn bạn đã gửi đánh giá!"
            } catch (e: Exception) {
                _message.value = "Lỗi gửi đánh giá: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
