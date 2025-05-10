package com.example.appmuabandocu.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val productId: String = "",
    val productName: String = "",
    val productPrice: String = "",
    val productImage: String = "",
    val read: Boolean = false
)
