package com.example.appmuabandocu.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appmuabandocu.model.AppNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("notifications")

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        db.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AppNotification>()
                snapshot.children.forEach { child ->
                    val notif = child.getValue(AppNotification::class.java)
                    if (notif != null) {
                        list.add(notif)
                    }
                }

                // Nếu chưa có thông báo nào từ Firebase, tạo một số thông báo hệ thống chào mừng
                if (list.isEmpty()) {
                    val defaultNotifs = listOf(
                        AppNotification(
                            id = "default_welcome",
                            userId = userId,
                            title = "Chào mừng bạn đến với Chợ Đồ Cũ!",
                            message = "Chúc bạn mua may bán đắt! Khám phá ngay hàng ngàn món đồ cũ giá tốt xung quanh bạn.",
                            type = "SYSTEM",
                            timestamp = System.currentTimeMillis() - 3600_000L,
                            isRead = false
                        ),
                        AppNotification(
                            id = "default_safety",
                            userId = userId,
                            title = "Mẹo giao dịch an toàn",
                            message = "Khuyến nghị gặp mặt trực tiếp tại nơi công cộng đông người để kiểm tra đồ trước khi thanh toán. Không chuyển cọc trước!",
                            type = "SYSTEM",
                            timestamp = System.currentTimeMillis() - 7200_000L,
                            isRead = true
                        )
                    )
                    _notifications.value = defaultNotifs
                } else {
                    _notifications.value = list.sortedByDescending { it.timestamp }
                }
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
            }
        })
    }

    fun markAsRead(notification: AppNotification) {
        val userId = auth.currentUser?.uid ?: return
        if (notification.id.startsWith("default_")) {
            _notifications.value = _notifications.value.map {
                if (it.id == notification.id) it.copy(isRead = true) else it
            }
            return
        }
        db.child(userId).child(notification.id).child("isRead").setValue(true)
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value.forEach { notif ->
            if (!notif.id.startsWith("default_")) {
                db.child(userId).child(notif.id).child("isRead").setValue(true)
            }
        }
    }

    fun deleteNotification(notification: AppNotification) {
        val userId = auth.currentUser?.uid ?: return
        if (notification.id.startsWith("default_")) {
            _notifications.value = _notifications.value.filter { it.id != notification.id }
            return
        }
        db.child(userId).child(notification.id).removeValue()
    }
}
