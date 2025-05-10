package com.example.appmuabandocu.feature_notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.AppNotification
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.ui.theme.Coral_Orange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.appmuabandocu.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "CHAT", "SYSTEM"

    val unreadCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "CHAT" -> notifications.filter { it.type == "CHAT" || it.type == "REVIEW" || it.type == "BUMP" }
            "SYSTEM" -> notifications.filter { it.type == "SYSTEM" || it.type == "REPORT" || it.type == "BAN" }
            else -> notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thông Báo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount thông báo chưa đọc",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đã đọc tất cả",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_text)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Tất cả (${notifications.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue_text,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "CHAT",
                        onClick = { selectedFilter = "CHAT" },
                        label = { Text("Tin nhắn & Giao dịch") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue_text,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "SYSTEM",
                        onClick = { selectedFilter = "SYSTEM" },
                        label = { Text("Hệ thống & An toàn") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue_text,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (isLoading && notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue_text)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Blue_text.copy(alpha = 0.1f),
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    modifier = Modifier.size(46.dp),
                                    tint = Blue_text
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Chưa có thông báo nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Khi có tin nhắn mới, đánh giá từ người mua hoặc thông báo từ Ban quản trị, chúng sẽ xuất hiện tại đây.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onClick = {
                                viewModel.markAsRead(notif)
                                when (notif.type) {
                                    "CHAT" -> navController.navigate(Screen.ChatList.route)
                                    "REVIEW" -> {
                                        if (notif.relatedId.isNotEmpty()) {
                                            navController.navigate(Screen.Reviews.createRoute(notif.relatedId, "Người bán"))
                                        }
                                    }
                                    "BUMP", "PRODUCT" -> {
                                        if (notif.relatedId.isNotEmpty()) {
                                            navController.navigate(Screen.ProductDetail.createRoute(notif.relatedId))
                                        } else {
                                            navController.navigate(Screen.ManageProduct.route)
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            onDelete = { viewModel.deleteNotification(notif) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isUnread = !notification.isRead

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xFFFFFFFF) else Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon theo loại
            val (icon, bgCol, tintCol) = getNotificationIconConfig(notification.type)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgCol),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintCol,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isUnread) Color(0xFF0F172A) else Color(0xFF334155),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Coral_Orange)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (isUnread) Color(0xFF334155) else Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa thông báo",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatNotificationTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Vừa xong"
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 30 -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        days > 0 -> "$days ngày trước"
        hours > 0 -> "$hours giờ trước"
        minutes > 0 -> "$minutes phút trước"
        else -> "Vừa xong"
    }
}

fun getNotificationIconConfig(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        "CHAT" -> Triple(Icons.Default.ChatBubble, Color(0xFFE0F2FE), Blue_text)
        "REVIEW" -> Triple(Icons.Default.Star, Color(0xFFFEF3C7), Color(0xFFD97706))
        "BUMP" -> Triple(Icons.Default.RocketLaunch, Color(0xFFFFEDE6), Coral_Orange)
        "REPORT", "BAN" -> Triple(Icons.Default.Warning, Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Triple(Icons.Default.Campaign, Color(0xFFEEF2FF), Blue_text)
    }
}
