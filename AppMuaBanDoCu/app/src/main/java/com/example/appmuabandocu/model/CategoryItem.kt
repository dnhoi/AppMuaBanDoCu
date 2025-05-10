package com.example.appmuabandocu.model

data class CategoryItem(
    val id: String = "",
    val name: String = "",
    val iconName: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

