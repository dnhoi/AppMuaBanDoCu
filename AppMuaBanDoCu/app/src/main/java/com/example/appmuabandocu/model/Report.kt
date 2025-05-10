package com.example.appmuabandocu.model

data class Report(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val details: String = "",
    val timestamp: Long = 0L,
    val status: String = "pending" // pending, resolved, dismissed
)
