package com.example.appmuabandocu.model

data class BannerItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val linkTarget: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
