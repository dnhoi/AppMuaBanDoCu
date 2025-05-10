package com.example.appmuabandocu.model

import java.util.Date

data class Product(
    var id: String = "",
    val productName: String = "",
    val status: String = "",
    val price: String = "",
    val address: String = "",
    val provinceFB: String = "",
    val numberUser: String = "",
    val category: String = "",
    val details: String = "",
    val negotiable: Boolean = false,
    val freeShip: Boolean = false,
    val imageUrl: String = "", // ảnh bìa
    val imageMota: List<String> = emptyList(), // các ảnh mô tả
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val timestamp: Long = Date().time, // sẽ được ghi đè trong ViewModel
    val inStock: Boolean = true,  // sẽ được ghi đè trong ViewModel
    val displayed: Boolean? = true,
    val lastBumpedAt: Long = 0L
)