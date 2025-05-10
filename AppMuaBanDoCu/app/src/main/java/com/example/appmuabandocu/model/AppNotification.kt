package com.example.appmuabandocu.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM", // "CHAT", "REVIEW", "BUMP", "SYSTEM", "REPORT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedId: String = "" // Product ID or Conversation ID
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "relatedId" to relatedId
        )
    }
}
