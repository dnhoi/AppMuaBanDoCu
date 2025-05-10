package com.example.appmuabandocu.model

data class ChatConversation(
    val id: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val productId: String = "",
    val productName: String = ""
)
