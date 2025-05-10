package com.example.appmuabandocu.model

data class Review(
    val id: String = "",
    val sellerId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val reviewerAvatar: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val timestamp: Long = 0L
)
