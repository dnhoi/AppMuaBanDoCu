package com.example.appmuabandocu.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ==========================================
// HỆ THỐNG MÀU CHUẨN GIAO DIỆN (60 - 30 - 10)
// ==========================================

// 1. Màu chủ đạo (60%): Trắng tinh (#FFFFFF)
// Dùng cho toàn bộ nền chính, card, surface, dialogs, bottom sheets
val White = Color(0xFFFFFFFF)
val Background_Light = Color(0xFFFFFFFF)       // Nền chính trắng tinh (#FFFFFF)
val Surface_Light = Color(0xFFF8FAFC)          // Trắng xám tối giản nhẹ
val Divider_Color = Color(0xFFE2E8F0)

// 2. Màu thương hiệu (30%): Xanh dương Navy (#1E3A8A hoặc #0D6EFD)
// Dùng cho TopAppBar, thanh tiêu đề, nút hành động chính, active navigation, icons thương hiệu
val Blue_text = Color(0xFF1E3A8A)              // Xanh dương Navy thương hiệu (#1E3A8A)
val Blue_text_Dark = Color(0xFF172554)         // Xanh Navy đậm (#172554) cho Admin / Topbars
val Blue_Navy = Color(0xFF1E3A8A)              // Alias Navy
val Blue_Brand_Light = Color(0xFF0D6EFD)       // Xanh dương sáng (#0D6EFD)

// 3. Màu nhấn (10%): Cam san hô (#FF5722)
// Dùng làm điểm nhấn thị giác: Giá tiền sản phẩm, badge hot / sale / giảm giá, cờ báo vi phạm, nút kêu gọi hành động
val orange = Color(0xFFFF5722)                 // Cam san hô (#FF5722) - Giữ tên 'orange' tương thích mã nguồn cũ
val Coral_Orange = Color(0xFFFF5722)           // Cam san hô nhấn (#FF5722)
val Coral_Orange_Light = Color(0xFFFF8A65)     // Cam san hô sáng

// Trạng thái sản phẩm
val Green_Available = Color(0xFF10B981)        // Còn hàng
val Red_Sold = Color(0xFFEF4444)               // Đã bán
val Gray_Hidden = Color(0xFF94A3B8)            // Đã ẩn