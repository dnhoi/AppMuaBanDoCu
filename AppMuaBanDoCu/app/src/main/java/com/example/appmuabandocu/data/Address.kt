package com.example.appmuabandocu.data

// Lớp Province
data class Province(
    val name: String,  // Tên tỉnh/thành phố
    val code: String,
    val districts: List<District>?
)

// Lớp District (Quận/Huyện)
data class District(
    val name: String,  // Tên quận/huyện
    val code: String,
    val wards: List<Ward>?
)

// Lớp Ward (Phường/Xã)
data class Ward(
    val name: String,  // Tên phường/xã
    val code: String   // Mã phường/xã
)

