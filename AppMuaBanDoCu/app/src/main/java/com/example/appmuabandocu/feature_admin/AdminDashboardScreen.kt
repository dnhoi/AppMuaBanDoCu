package com.example.appmuabandocu.feature_admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.BannerItem
import com.example.appmuabandocu.model.CategoryItem
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.ui.theme.*
import com.example.appmuabandocu.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val productList = viewModel.productList
    val userList = viewModel.userList
    val reportList = viewModel.reportList
    val bannerList = viewModel.bannerList
    val categoryList = viewModel.categoryList
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Bài Đăng (${productList.size})",
        "Người Dùng (${userList.size})",
        "Báo Cáo (${reportList.size})",
        "Banners (${bannerList.size})",
        "Danh Mục (${categoryList.size})",
        "Thống Kê"
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var userToBan by remember { mutableStateOf<User?>(null) }
    var banDurationDays by remember { mutableIntStateOf(3) } // 0 = Warning, 3 = 3 days, 7 = 7 days, -1 = Permanent
    var banReasonOption by remember { mutableStateOf("Lừa đảo tiền cọc / hàng giả") }
    var customBanReason by remember { mutableStateOf("") }
    var userToUnban by remember { mutableStateOf<User?>(null) }
    var userSubFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "BANNED"

    var showAddBannerDialog by remember { mutableStateOf(false) }
    var bannerTitle by remember { mutableStateOf("") }
    var bannerSubtitle by remember { mutableStateOf("") }
    var bannerImageUrl by remember { mutableStateOf("") }
    var bannerLinkTarget by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // Confirmation dialog for deleting product
    viewModel.productToDelete.value?.let { product ->
        AlertDialog(
            onDismissRequest = { viewModel.productToDelete.value = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Xóa bài viết vi phạm?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa bài viết \"${product.productName ?: "Sản phẩm"}\" của người dùng \"${product.userName ?: "Không rõ"}\"?\n\nHành động này sẽ xóa vĩnh viễn khỏi toàn bộ hệ thống!"
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteProduct(product) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Xóa vĩnh viễn", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.productToDelete.value = null }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }

    // Confirmation dialog for toggling user admin role
    viewModel.userToToggleRole.value?.let { targetUser ->
        val willBeAdmin = !targetUser.isAdmin
        AlertDialog(
            onDismissRequest = { viewModel.userToToggleRole.value = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Blue_text,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (willBeAdmin) "Cấp quyền Quản Trị Viên?" else "Thu hồi quyền Quản Trị Viên?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (willBeAdmin)
                        "Cấp quyền Admin cho tài khoản \"${targetUser.fullName.ifEmpty { targetUser.email }}\"? Tài khoản này sẽ có toàn quyền kiểm duyệt bài viết và quản lý người dùng."
                    else
                        "Thu hồi quyền Admin của \"${targetUser.fullName.ifEmpty { targetUser.email }}\"? Tài khoản này sẽ trở về quyền người dùng thông thường."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.toggleAdminRole(targetUser) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (willBeAdmin) Blue_text else Color(0xFFFF5252))
                ) {
                    Text(if (willBeAdmin) "Cấp quyền Admin" else "Thu hồi quyền", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.userToToggleRole.value = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog xác nhận Kỷ luật / Khóa tài khoản
    userToBan?.let { targetUser ->
        val reasons = listOf(
            "Lừa đảo tiền cọc / hàng giả",
            "Đăng bài sai sự thật / sai mô tả",
            "Đăng bán mặt hàng cấm kinh doanh",
            "Sử dụng ngôn từ xúc phạm / thô tục",
            "Spam tin nhắn / Nhiều người dùng báo cáo",
            "Lý do khác (nhập bên dưới)"
        )

        AlertDialog(
            onDismissRequest = { userToBan = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Xử lý kỷ luật tài khoản",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tài khoản: \"${targetUser.fullName.ifEmpty { targetUser.email }}\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Chọn mức phạt kỷ luật:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = banDurationDays == 0,
                            onClick = { banDurationDays = 0 },
                            label = { Text("Cảnh cáo", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF9800),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = banDurationDays == 3,
                            onClick = { banDurationDays = 3 },
                            label = { Text("Khóa 3 ngày", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE53935),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = banDurationDays == 7,
                            onClick = { banDurationDays = 7 },
                            label = { Text("Khóa 7 ngày", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD32F2F),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = banDurationDays == -1,
                            onClick = { banDurationDays = -1 },
                            label = { Text("Vĩnh viễn", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFB71C1C),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "2. Chọn lý do vi phạm:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    reasons.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { banReasonOption = r }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = banReasonOption == r,
                                onClick = { banReasonOption = r },
                                colors = RadioButtonDefaults.colors(selectedColor = Blue_text)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = r, fontSize = 12.sp)
                        }
                    }

                    if (banReasonOption == "Lý do khác (nhập bên dưới)") {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customBanReason,
                            onValueChange = { customBanReason = it },
                            placeholder = { Text("Nhập lý do vi phạm cụ thể...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (banReasonOption == "Lý do khác (nhập bên dưới)") {
                            customBanReason.ifEmpty { "Vi phạm tiêu chuẩn cộng đồng" }
                        } else banReasonOption
                        viewModel.banUserWithDuration(targetUser, finalReason, banDurationDays)
                        userToBan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (banDurationDays == 0) Color(0xFFFF9800) else Color(0xFFD32F2F))
                ) {
                    Text(if (banDurationDays == 0) "Gửi Cảnh Cáo" else "Xác Nhận Khóa", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToBan = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Thêm Banner Mới
    if (showAddBannerDialog) {
        AlertDialog(
            onDismissRequest = { showAddBannerDialog = false },
            title = { Text("Thêm Banner Quảng Cáo Mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Tiêu đề banner (Ví dụ: Siêu sale đồ cũ)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerSubtitle,
                        onValueChange = { bannerSubtitle = it },
                        label = { Text("Phụ đề (Ví dụ: Giảm giá đến 50% thiết bị điện tử)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerImageUrl,
                        onValueChange = { bannerImageUrl = it },
                        label = { Text("URL hình ảnh banner") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bannerLinkTarget,
                        onValueChange = { bannerLinkTarget = it },
                        label = { Text("Danh mục liên kết (Ví dụ: Thiết bị điện tử)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bannerTitle.isNotBlank()) {
                            viewModel.addBanner(bannerTitle, bannerSubtitle, bannerImageUrl, bannerLinkTarget)
                            showAddBannerDialog = false
                            bannerTitle = ""
                            bannerSubtitle = ""
                            bannerImageUrl = ""
                            bannerLinkTarget = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text)
                ) {
                    Text("Lưu Banner", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddBannerDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Thêm Danh Mục Mới
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Thêm Danh Mục Sản Phẩm Mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Danh mục mới sẽ được lưu vào hệ thống và hiển thị ngay trên ứng dụng cho toàn bộ người dùng mà không cần cập nhật lại app.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Tên danh mục (Ví dụ: Thú cưng & Phụ kiện)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName)
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text)
                ) {
                    Text("Thêm danh mục", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog xác nhận Mở khóa tài khoản
    userToUnban?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userToUnban = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Mở khóa tài khoản",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn mở khóa cho tài khoản \"${targetUser.fullName.ifEmpty { targetUser.email }}\"? Người dùng sẽ có thể đăng bài và mua bán bình thường trở lại."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unbanUser(targetUser)
                        userToUnban = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Mở khóa", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToUnban = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue_text_Dark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background_Light)
        ) {
            // Overview summary cards
            AdminOverviewStats(productList = productList, userList = userList, reportCount = reportList.size)

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        when (selectedTabIndex) {
                            0 -> "Tìm sản phẩm, người bán, danh mục..."
                            1 -> "Tìm theo tên người dùng, email, SĐT..."
                            2 -> "Tìm theo tên bài, người bán, lý do vi phạm..."
                            else -> "Tìm kiếm thống kê..."
                        }
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Color.Gray)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Blue_text,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Blue_text else Color.Gray
                            )
                        }
                    )
                }
            }

            if (isLoading && productList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue_text)
                }
            } else {
                when (selectedTabIndex) {
                    0 -> {
                        // Product Moderation Tab
                        val filteredProducts = remember(productList, searchQuery) {
                            val q = searchQuery.trim().lowercase(Locale.getDefault())
                            if (q.isEmpty()) productList
                            else productList.filter {
                                (it.productName ?: "").lowercase(Locale.getDefault()).contains(q) ||
                                (it.userName ?: "").lowercase(Locale.getDefault()).contains(q) ||
                                (it.category ?: "").lowercase(Locale.getDefault()).contains(q) ||
                                (it.address ?: "").lowercase(Locale.getDefault()).contains(q)
                            }
                        }

                        if (filteredProducts.isEmpty()) {
                            AdminEmptyView(message = "Không có bài viết nào phù hợp")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredProducts, key = { it.id.ifEmpty { it.hashCode().toString() } }) { product ->
                                    AdminProductCard(
                                        product = product,
                                        onViewDetail = {
                                            if (product.id.isNotEmpty()) {
                                                navController.navigate(Screen.ProductDetail.createRoute(product.id))
                                            }
                                        },
                                        onToggleVisibility = {
                                            viewModel.toggleProductVisibility(product)
                                        },
                                        onDelete = {
                                            viewModel.productToDelete.value = product
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // User Management Tab
                        val filteredUsers = remember(userList, searchQuery, userSubFilter) {
                            val q = searchQuery.trim().lowercase(Locale.getDefault())
                            val base = when (userSubFilter) {
                                "ACTIVE" -> userList.filter { !it.isBanned }
                                "BANNED" -> userList.filter { it.isBanned }
                                else -> userList
                            }
                            if (q.isEmpty()) base
                            else base.filter {
                                it.fullName.lowercase(Locale.getDefault()).contains(q) ||
                                it.email.lowercase(Locale.getDefault()).contains(q) ||
                                (it.phoneNumber ?: "").contains(q)
                            }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub-filter chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = userSubFilter == "ALL",
                                    onClick = { userSubFilter = "ALL" },
                                    label = { Text("Tất cả (${userList.size})", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = userSubFilter == "ACTIVE",
                                    onClick = { userSubFilter = "ACTIVE" },
                                    label = { Text("Hoạt động (${userList.count { !it.isBanned }})", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = userSubFilter == "BANNED",
                                    onClick = { userSubFilter = "BANNED" },
                                    label = { Text("Đã khóa (${userList.count { it.isBanned }})", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD32F2F),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }

                            if (filteredUsers.isEmpty()) {
                                AdminEmptyView(message = "Không tìm thấy người dùng nào phù hợp")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredUsers, key = { it.uid.ifEmpty { it.email } }) { targetUser ->
                                        AdminUserCard(
                                            user = targetUser,
                                            onViewProfile = {
                                                if (targetUser.uid.isNotEmpty()) {
                                                    navController.navigate(Screen.ProfileUser.createRoute(targetUser.uid))
                                                }
                                            },
                                            onToggleAdminRole = {
                                                viewModel.userToToggleRole.value = targetUser
                                            },
                                            onToggleBan = {
                                                if (targetUser.isBanned) {
                                                    userToUnban = targetUser
                                                } else {
                                                    userToBan = targetUser
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // User Reports Moderation Tab
                        val filteredReports = remember(reportList, searchQuery) {
                            val q = searchQuery.trim().lowercase(Locale.getDefault())
                            if (q.isEmpty()) reportList
                            else reportList.filter {
                                it.productName.lowercase(Locale.getDefault()).contains(q) ||
                                it.sellerName.lowercase(Locale.getDefault()).contains(q) ||
                                it.reason.lowercase(Locale.getDefault()).contains(q) ||
                                it.reporterName.lowercase(Locale.getDefault()).contains(q) ||
                                it.details.lowercase(Locale.getDefault()).contains(q)
                            }
                        }

                        if (filteredReports.isEmpty()) {
                            AdminEmptyView(message = "Không có bài viết nào bị báo cáo vi phạm")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredReports, key = { it.id.ifEmpty { it.hashCode().toString() } }) { report ->
                                    AdminReportCard(
                                        report = report,
                                        onViewProduct = {
                                            if (report.productId.isNotEmpty()) {
                                                navController.navigate(Screen.ProductDetail.createRoute(report.productId))
                                            }
                                        },
                                        onDismiss = {
                                            viewModel.dismissReport(report)
                                        },
                                        onDeletePost = {
                                            viewModel.deleteReportedProduct(report)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // Banner Management Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Banners Đang Hoạt Động (${bannerList.count { it.isActive }}/${bannerList.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue_text_Dark
                                )
                                Button(
                                    onClick = { showAddBannerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm Banner", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            if (bannerList.isEmpty()) {
                                AdminEmptyView(message = "Chưa có banner quảng cáo nào. Bấm 'Thêm Banner' để bắt đầu.")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(bannerList, key = { it.id }) { banner ->
                                        AdminBannerCard(
                                            banner = banner,
                                            onToggleActive = { viewModel.toggleBannerActive(banner) },
                                            onDelete = { viewModel.deleteBanner(banner) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Dynamic Categories Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Danh Mục Trực Tuyến (${categoryList.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue_text_Dark
                                )
                                Button(
                                    onClick = { showAddCategoryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Thêm Danh Mục", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            if (categoryList.isEmpty()) {
                                AdminEmptyView(message = "Chưa có danh mục trực tuyến nào. Bấm 'Thêm Danh Mục' để bổ sung.")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(categoryList, key = { it.id }) { cat ->
                                        AdminCategoryCard(
                                            category = cat,
                                            onDelete = { viewModel.deleteCategory(cat) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // Statistics & Visual Charts Tab
                        AdminStatisticsTab(productList = productList, userList = userList)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOverviewStats(
    productList: List<Product>,
    userList: List<User>,
    reportCount: Int = 0
) {
    val totalProducts = productList.size
    val totalUsers = userList.size
    val soldCount = productList.count { it.status == "sold" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdminStatCard(
            title = "Tổng bài",
            count = totalProducts.toString(),
            color = Blue_text,
            icon = Icons.Default.ShoppingBag,
            modifier = Modifier.weight(1f)
        )
        AdminStatCard(
            title = "Người dùng",
            count = totalUsers.toString(),
            color = Color(0xFF673AB7),
            icon = Icons.Default.People,
            modifier = Modifier.weight(1f)
        )
        AdminStatCard(
            title = "Đã bán",
            count = soldCount.toString(),
            color = Green_Available,
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f)
        )
        AdminStatCard(
            title = "Báo cáo",
            count = reportCount.toString(),
            color = if (reportCount > 0) Color(0xFFD32F2F) else Color.Gray,
            icon = Icons.Default.Flag,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AdminProductCard(
    product: Product,
    onViewDetail: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit
) {
    val isHidden = product.displayed == false
    val formattedPrice = remember(product.price) {
        try {
            val num = product.price.toLongOrNull() ?: 0L
            NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(num) + " đ"
        } catch (_: Exception) {
            "${product.price} đ"
        }
    }

    val formattedDate = remember(product.timestamp) {
        if (product.timestamp > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(product.timestamp))
        } else "Không xác định"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    val imageUrl = product.imageUrl.ifEmpty { product.imageMota.firstOrNull() ?: "" }
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl.replace("http://", "https://"),
                            contentDescription = product.productName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.productName ?: "Không có tên",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isHidden -> Color(0xFFEEEEEE)
                                product.status == "sold" -> Red_Sold.copy(alpha = 0.15f)
                                else -> Green_Available.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when {
                                    isHidden -> "Đã ẩn"
                                    product.status == "sold" -> "Đã bán"
                                    else -> "Đang bán"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    isHidden -> Color.DarkGray
                                    product.status == "sold" -> Red_Sold
                                    else -> Green_Available
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formattedPrice,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.userName ?: "Người bán ẩn danh",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Ngày: $formattedDate • DM: ${product.category ?: "Khác"}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Detail Button
                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Blue_text
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chi tiết", fontSize = 12.sp, color = Blue_text)
                }

                // Hide/Show Toggle Button
                TextButton(
                    onClick = onToggleVisibility,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isHidden) Green_Available else Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHidden) "Hiện lại" else "Ẩn bài",
                        fontSize = 12.sp,
                        color = if (isHidden) Green_Available else Color(0xFFFF9800)
                    )
                }

                // Delete Button
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa bài", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: User,
    onViewProfile: () -> Unit,
    onToggleAdminRole: () -> Unit,
    onToggleBan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.avatarUrl.replace("http://", "https://"),
                            contentDescription = user.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.fullName.ifEmpty { "Chưa cập nhật tên" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Role badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (user.isAdmin) Color(0xFFFFD54F).copy(alpha = 0.25f) else Color(0xFFEEEEEE)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (user.isAdmin) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFFF57F17),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = if (user.isAdmin) "ADMIN" else "MEMBER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isAdmin) Color(0xFFE65100) else Color.Gray
                                    )
                                }
                            }

                            // Ban badge
                            if (user.isBanned) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "ĐÃ KHÓA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = user.email.ifEmpty { "Không có email" },
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!user.phoneNumber.isNullOrBlank()) {
                        Text(
                            text = "SĐT: ${user.phoneNumber}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    if (user.isBanned && user.banReason.isNotBlank()) {
                        Text(
                            text = "Lý do khóa: ${user.banReason}",
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (!user.province.isNullOrBlank() || !user.district.isNullOrBlank()) {
                        Text(
                            text = "Khu vực: ${listOfNotNull(user.district, user.province).joinToString(", ")}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            // User Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Blue_text
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hồ sơ", fontSize = 12.sp, color = Blue_text)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onToggleAdminRole,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isAdmin) Color(0xFFFF9800) else Blue_text
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (user.isAdmin) Icons.Default.RemoveModerator else Icons.Default.AddModerator,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (user.isAdmin) "Hạ quyền" else "Cấp Admin",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onToggleBan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) Green_Available else Color(0xFFDC2626)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (user.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (user.isBanned) "Mở khóa" else "Khóa TK",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatisticsTab(
    productList: List<Product>,
    userList: List<User>
) {
    val totalProducts = productList.size
    val soldCount = productList.count { it.status == "sold" }
    val availableCount = productList.count { it.status == "available" }
    val hiddenCount = productList.count { it.displayed == false }
    val totalAdmins = userList.count { it.isAdmin }
    val bannedUsersCount = userList.count { it.isBanned }
    val activeUsersCount = userList.size - bannedUsersCount

    // Categories breakdown sorted descending
    val categoryCounts = remember(productList) {
        productList.groupBy { it.category?.ifEmpty { "Khác" } ?: "Khác" }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    // Weekly posts calculation (last 7 days)
    val weeklyStats = remember(productList) {
        val cal = java.util.Calendar.getInstance()
        val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))
        val list = mutableListOf<Triple<String, String, Int>>() // DayName, DateStr, Count

        for (i in 6 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val startOfDay = c.apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

            val count = productList.count { p ->
                p.timestamp in startOfDay..endOfDay
            }
            val dayName = when (c.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "T2"
                java.util.Calendar.TUESDAY -> "T3"
                java.util.Calendar.WEDNESDAY -> "T4"
                java.util.Calendar.THURSDAY -> "T5"
                java.util.Calendar.FRIDAY -> "T6"
                java.util.Calendar.SATURDAY -> "T7"
                java.util.Calendar.SUNDAY -> "CN"
                else -> dayNameFormat.format(c.time)
            }
            list.add(Triple(dayName, dayFormat.format(c.time), count))
        }
        list
    }

    val maxDailyCount = remember(weeklyStats) {
        weeklyStats.maxOfOrNull { it.third }?.coerceAtLeast(1) ?: 1
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Biểu đồ 1: Số lượng bài đăng mới theo ngày trong tuần
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tần Suất Đăng Bài (7 Ngày Gần Nhất)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Blue_text_Dark
                        )
                        Text(
                            text = "${weeklyStats.sumOf { it.third }} bài mới",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue_text
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    // Biểu đồ cột trực quan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyStats.forEach { (dayName, dateStr, count) ->
                            val heightFraction = (count.toFloat() / maxDailyCount).coerceIn(0.06f, 1f)
                            val isMax = count == maxDailyCount && count > 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    fontWeight = if (isMax) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMax) Blue_text else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .fillMaxHeight(heightFraction * 0.75f)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (isMax) Blue_text else if (count > 0) Color(0xFF90CAF9) else Color(0xFFECEFF1)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isMax) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMax) Blue_text_Dark else Color.DarkGray
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Biểu đồ 2: Tỷ lệ danh mục hot nhất
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tỷ Lệ Danh Mục Nổi Bật Nhất",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Blue_text_Dark
                    )
                    Text(
                        text = "Thị phần mặt hàng đăng bán trên hệ thống",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (categoryCounts.isEmpty()) {
                        Text(text = "Chưa có dữ liệu danh mục", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        categoryCounts.take(6).forEachIndexed { index, (cat, count) ->
                            val percentage = if (totalProducts > 0) (count * 100 / totalProducts) else 0
                            val barColor = when (index) {
                                0 -> Blue_text
                                1 -> Color(0xFF26A69A)
                                2 -> Color(0xFFFF9800)
                                3 -> Color(0xFF7E57C2)
                                else -> Color(0xFF78909C)
                            }

                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(barColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = barColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = "$count bài ($percentage%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = barColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { if (totalProducts > 0) count.toFloat() / totalProducts else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = barColor,
                                    trackColor = Color(0xFFEEEEEE)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Biểu đồ 3: Tỷ lệ tài khoản hoạt động và bị khóa
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tỷ Lệ Tài Khoản Hoạt Động & Bị Khóa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Blue_text_Dark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val activePercent = if (userList.isNotEmpty()) (activeUsersCount * 100 / userList.size) else 100
                    val bannedPercent = 100 - activePercent

                    // Visual Progress Stacked Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                    ) {
                        if (activePercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(activePercent.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(Green_Available)
                            )
                        }
                        if (bannedPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(bannedPercent.toFloat().coerceAtLeast(1f))
                                    .fillMaxHeight()
                                    .background(Red_Sold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Green_Available)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hoạt động: $activeUsersCount ($activePercent%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Red_Sold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bị khóa: $bannedUsersCount ($bannedPercent%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }

        // Card 4: Tổng quan số liệu hệ thống chi tiết
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Số Liệu Tổng Hợp Hệ Thống",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Blue_text_Dark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatRowItem(label = "Tổng bài đăng trên sàn", value = "$totalProducts bài")
                    StatRowItem(label = "Bài viết đang hiển thị bán", value = "$availableCount bài", valueColor = Green_Available)
                    StatRowItem(label = "Bài viết đã hoàn thành giao dịch (Đã bán)", value = "$soldCount bài", valueColor = Red_Sold)
                    StatRowItem(label = "Bài viết bị ẩn vi phạm", value = "$hiddenCount bài", valueColor = Color(0xFFFF9800))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                    StatRowItem(label = "Tổng thành viên đăng ký", value = "${userList.size} tài khoản")
                    StatRowItem(label = "Số lượng Quản Trị Viên (Admin)", value = "$totalAdmins người", valueColor = Color(0xFFF57F17))
                }
            }
        }
    }
}

@Composable
fun StatRowItem(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun AdminEmptyView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AdminReportCard(
    report: com.example.appmuabandocu.model.Report,
    onViewProduct: () -> Unit,
    onDismiss: () -> Unit,
    onDeletePost: () -> Unit
) {
    val formattedDate = remember(report.timestamp) {
        if (report.timestamp > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp))
        } else "Mới đây"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Flag + Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFFFFEBEE), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Báo cáo vi phạm",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VI PHẠM: ${report.reason.uppercase()}",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Báo cáo bởi: ${report.reporterName} • $formattedDate",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product brief box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF9F9F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (report.productImage.isNotEmpty()) {
                        AsyncImage(
                            model = report.productImage.replace("http://", "https://"),
                            contentDescription = report.productName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.productName.ifEmpty { "Sản phẩm bị báo cáo" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Người bán: ${report.sellerName}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            if (report.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nội dung phản ánh: \"${report.details}\"",
                    fontSize = 12.sp,
                    color = Color(0xFF37474F),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewProduct,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Blue_text
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xem bài", fontSize = 12.sp, color = Blue_text)
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Bỏ qua", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onDeletePost,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa bài ngay", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminBannerCard(
    banner: BannerItem,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa Banner", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa banner '${banner.title}' không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Banner Image
            if (banner.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = banner.imageUrl.replace("http://", "https://"),
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Status Badge overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (banner.isActive) Green_Available else Color.Gray
                    ) {
                        Text(
                            text = if (banner.isActive) "Đang hiển thị" else "Đã tắt",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = banner.title.ifEmpty { "Banner không có tiêu đề" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Blue_text_Dark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (banner.subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banner.subtitle,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (banner.linkTarget.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Blue_text
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Liên kết: ${banner.linkTarget}",
                                fontSize = 11.sp,
                                color = Blue_text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch / Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleActive() }
                ) {
                    Switch(
                        checked = banner.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Green_Available
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (banner.isActive) "Hiển thị trên Trang chủ" else "Tạm tắt quảng cáo",
                        fontSize = 12.sp,
                        color = if (banner.isActive) Color.DarkGray else Color.Gray
                    )
                }

                // Delete Button
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa banner",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminCategoryCard(
    category: CategoryItem,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa Danh Mục", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục '${category.name}' không? Các sản phẩm thuộc danh mục này vẫn sẽ được giữ lại.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Blue_text.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Blue_text,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Blue_text_Dark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = "Online",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Blue_text,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (category.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Xóa danh mục",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}


