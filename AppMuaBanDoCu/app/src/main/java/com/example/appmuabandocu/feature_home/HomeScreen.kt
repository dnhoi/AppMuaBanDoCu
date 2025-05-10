package com.example.appmuabandocu.feature_home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.RangeSlider

import com.example.appmuabandocu.model.BannerItem
import com.example.appmuabandocu.model.CategoryItem
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appmuabandocu.R
import com.example.appmuabandocu.ui.theme.Blue_text
import coil.compose.AsyncImage
import com.example.appmuabandocu.component.FeatureInDevelopmentDialog
import com.example.appmuabandocu.core.navigation.BottomNavBar
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.ui.theme.orange
import com.example.appmuabandocu.ui.theme.Coral_Orange
import com.example.appmuabandocu.viewmodel.FavoriteViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel(),
) {

    val products = productViewModel.loadVisibleProducts()

    val searchResults by productViewModel.searchResults.collectAsState<List<Product>>()
    var query by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    // Lịch sử tìm kiếm gần đây
    val sharedPrefs = remember { context.getSharedPreferences("app_search_prefs", android.content.Context.MODE_PRIVATE) }
    var recentSearches by remember {
        val saved = sharedPrefs.getStringSet("recent_history", null)?.toList()
            ?: listOf("iPhone", "Xe máy", "Bàn ghế gỗ", "Nồi cơm")
        mutableStateOf(saved)
    }

    fun saveSearchQuery(q: String) {
        val trimmed = q.trim()
        if (trimmed.length >= 2) {
            val updated = (listOf(trimmed) + recentSearches.filter { it != trimmed }).take(6)
            recentSearches = updated
            sharedPrefs.edit().putStringSet("recent_history", updated.toSet()).apply()
        }
    }

    fun clearRecentSearches() {
        recentSearches = emptyList()
        sharedPrefs.edit().remove("recent_history").apply()
    }

    // Đồ cũ gần tôi state
    var isNearMeActive by remember { mutableStateOf(false) }
    var userCity by remember { mutableStateOf("Hà Nội") }

    val bannerList by productViewModel.bannerList.collectAsState()
    val dynamicCategoryList by productViewModel.categoryList.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Advanced Filter states
    var showFilterSheet by remember { mutableStateOf(false) }
    var priceRange by remember { mutableStateOf(0f..20_000_000f) }
    var selectedCondition by remember { mutableStateOf("Tất cả") }
    var selectedProvince by remember { mutableStateOf("Tất cả") }
    var sortOption by remember { mutableStateOf("newest") } // "newest", "price_asc", "price_desc", "rating_desc"

    val conditions = listOf("Tất cả", "Mới 99%", "Đã qua sử dụng", "Hoạt động tốt")
    val provinces = listOf(
        "Tất cả", "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ",
        "Hải Phòng", "Bình Dương", "Đồng Nai", "Quảng Ninh", "Khánh Hòa"
    )

    val isFilterActive = priceRange.start > 0f || priceRange.endInclusive < 20_000_000f || selectedCondition != "Tất cả" || selectedProvince != "Tất cả" || sortOption != "newest" || isNearMeActive

    // Base products (search or full list)
    val baseProducts = if (query.isNotBlank()) searchResults else products

    // Filter by Category, Price range, Condition, Province, Near Me
    val filteredProducts = baseProducts.filter { product ->
        val matchCategory = selectedCategory == null || product.category == selectedCategory
        val priceNum = try { product.price.toDouble() } catch (_: Exception) { 0.0 }
        val matchPrice = (priceNum >= priceRange.start) && (priceRange.endInclusive >= 20_000_000f || priceNum <= priceRange.endInclusive)
        val matchCondition = if (selectedCondition == "Tất cả") true else {
            (product.details ?: "").contains(selectedCondition, ignoreCase = true) ||
            (product.status ?: "").contains(selectedCondition, ignoreCase = true) ||
            (product.productName ?: "").contains(selectedCondition, ignoreCase = true)
        }
        val matchLocation = if (selectedProvince == "Tất cả") true else {
            (product.address ?: "").contains(selectedProvince, ignoreCase = true) || (product.provinceFB ?: "").contains(selectedProvince, ignoreCase = true)
        }
        val matchNearMe = if (!isNearMeActive) true else {
            (product.address ?: "").contains(userCity, ignoreCase = true) || (product.provinceFB ?: "").contains(userCity, ignoreCase = true)
        }
        matchCategory && matchPrice && matchCondition && matchLocation && matchNearMe
    }

    // Sort order
    val sortedProducts = when (sortOption) {
        "price_asc" -> filteredProducts.sortedBy { try { it.price.toDouble() } catch (_: Exception) { 0.0 } }
        "price_desc" -> filteredProducts.sortedByDescending { try { it.price.toDouble() } catch (_: Exception) { 0.0 } }
        "rating_desc" -> filteredProducts.sortedByDescending { it.lastBumpedAt.coerceAtLeast(it.timestamp) }
        else -> filteredProducts.sortedByDescending { it.timestamp }
    }

    // Thêm ScrollState để theo dõi trạng thái cuộn
    val scrollState = rememberScrollState()

    // Biến để kiểm soát hiển thị phần đầu
    var isHeaderVisible by remember { mutableStateOf(true) }

    var previousScrollOffset by remember { mutableIntStateOf(0) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Xác định hướng cuộn
                val delta = available.y

                // Cập nhật trạng thái hiển thị header dựa trên hướng cuộn
                // Cuộn xuống (delta < 0) -> ẩn header
                // Cuộn lên (delta > 0) -> hiện header
                if (delta < -10) { // Ngưỡng để tránh thay đổi liên tục
                    isHeaderVisible = false
                } else if (delta > 10) {
                    isHeaderVisible = true
                }

                return Offset.Zero
            }
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return Offset.Zero // Không tiêu thụ bất kỳ cuộn nào
            }
        }
    }

    // Log để kiểm tra danh sách sản phẩm
    LaunchedEffect(products) {
        Log.d("HomeScreen", "Sản phẩm: ${products.size} sản phẩm")
    }

    var showDevelopmentDialog by remember { mutableStateOf(false) }
    if (showDevelopmentDialog) {
        FeatureInDevelopmentDialog(
            onDismiss = { showDevelopmentDialog = false },
            featureName = "Tính năng" // Tùy chỉnh tên tính năng
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thanh Lý Nhanh",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue_text
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 2.dp)
                                    .size(8.dp)
                                    .background(Coral_Orange, shape = CircleShape)
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.ChatList.route) }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Messages",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        // Advanced Filter Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bộ Lọc Tìm Kiếm Nâng Cao",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Blue_text
                        )
                        androidx.compose.material3.TextButton(onClick = {
                            priceRange = 0f..20_000_000f
                            selectedCondition = "Tất cả"
                            selectedProvince = "Tất cả"
                            sortOption = "newest"
                        }) {
                            Text("Đặt lại", color = Color(0xFFFF5722), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Khoảng giá (RangeSlider)
                    Text(
                        text = "Khoảng giá sản phẩm:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    val priceLabel = if (priceRange.start <= 0f && priceRange.endInclusive >= 20_000_000f) {
                        "Tất cả mức giá (0 đ - Không giới hạn)"
                    } else {
                        val minF = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(priceRange.start.toLong())
                        val maxF = if (priceRange.endInclusive >= 20_000_000f) "trên 20.000.000 đ" else "${NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(priceRange.endInclusive.toLong())} đ"
                        "Từ $minF đ đến $maxF"
                    }
                    Text(
                        text = priceLabel,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    RangeSlider(
                        value = priceRange,
                        onValueChange = { priceRange = it },
                        valueRange = 0f..20_000_000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = Blue_text,
                            activeTrackColor = Blue_text,
                            inactiveTrackColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Tình trạng hàng
                    Text(
                        text = "Tình trạng mặt hàng:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(conditions.size) { idx ->
                            val cond = conditions[idx]
                            val isSelected = selectedCondition == cond
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCondition = cond },
                                label = { Text(cond, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Khu vực / Tỉnh thành
                    Text(
                        text = "Tỉnh / Thành phố giao dịch:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(provinces.size) { idx ->
                            val prov = provinces[idx]
                            val isSelected = selectedProvince == prov
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedProvince = prov },
                                label = { Text(prov, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Sắp xếp
                    Text(
                        text = "Sắp xếp theo:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = sortOption == "newest",
                                onClick = { sortOption = "newest" },
                                label = { Text("Mới nhất", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = sortOption == "price_asc",
                                onClick = { sortOption = "price_asc" },
                                label = { Text("Giá thấp -> cao", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = sortOption == "price_desc",
                                onClick = { sortOption = "price_desc" },
                                label = { Text("Giá cao -> thấp", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = sortOption == "rating_desc",
                                onClick = { sortOption = "rating_desc" },
                                label = { Text("Đánh giá cao nhất", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Apply button
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue_text),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Áp dụng (${sortedProducts.size} sản phẩm)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                )
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus() // thoát focus khi click ra ngoài
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = slideInVertically() + expandVertically(),
                    exit = slideOutVertically() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Blue_text,
                                    modifier = Modifier.padding(start = 8.dp)
                                )

                                BasicTextField(
                                    value = query,
                                    onValueChange = {
                                        query = it
                                        productViewModel.searchProducts(it)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (query.isEmpty()) {
                                                Text(
                                                    text = "Bạn muốn mua gì?",
                                                    fontSize = 16.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    singleLine = true,
                                    cursorBrush = SolidColor(Blue_text),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        saveSearchQuery(query)
                                        focusManager.clearFocus()
                                    })
                                )

                                Box(contentAlignment = Alignment.Center) {
                                    IconButton(
                                        onClick = { showFilterSheet = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Filter",
                                            tint = if (isFilterActive) Color(0xFFFF5722) else Blue_text
                                        )
                                    }
                                    if (isFilterActive) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(top = 4.dp, end = 4.dp)
                                                .size(8.dp)
                                                .background(Color(0xFFFF5722), shape = CircleShape)
                                        )
                                    }
                                }
                            }
                        }

                        // Lịch sử tìm kiếm gần đây
                        if (recentSearches.isNotEmpty() && query.isEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tìm kiếm gần đây",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "Xóa lịch sử",
                                    fontSize = 11.sp,
                                    color = Coral_Orange,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { clearRecentSearches() }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(recentSearches) { itemQuery ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable {
                                            query = itemQuery
                                            productViewModel.searchProducts(itemQuery)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = itemQuery,
                                                fontSize = 12.sp,
                                                color = Color(0xFF334155),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Đồ cũ gần tôi Bar
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isNearMeActive,
                                onClick = { isNearMeActive = !isNearMeActive },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isNearMeActive) Color.White else Coral_Orange
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (isNearMeActive) "Đang tìm gần tôi ($userCity)" else "Đồ cũ gần tôi",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Coral_Orange,
                                    selectedLabelColor = Color.White
                                )
                            )

                            if (isNearMeActive) {
                                TextButton(onClick = {
                                    userCity = when (userCity) {
                                        "Hà Nội" -> "Hồ Chí Minh"
                                        "Hồ Chí Minh" -> "Đà Nẵng"
                                        "Đà Nẵng" -> "Cần Thơ"
                                        else -> "Hà Nội"
                                    }
                                }) {
                                    Text(
                                        text = "Đổi vị trí ($userCity)",
                                        fontSize = 11.sp,
                                        color = Blue_text,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Banners Section
                        Spacer(modifier = Modifier.height(14.dp))
                        if (bannerList.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(bannerList) { banner ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clickable {
                                                if (banner.linkTarget.isNotBlank()) {
                                                    selectedCategory = banner.linkTarget
                                                }
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (banner.imageUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = banner.imageUrl,
                                                    contentDescription = banner.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                                            )
                                                        )
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                                colors = listOf(Blue_text, Color(0xFF1E3A8A))
                                                            )
                                                        )
                                                )
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .padding(14.dp)
                                                    .align(Alignment.CenterStart)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Coral_Orange
                                                ) {
                                                    Text(
                                                        text = "NỔI BẬT",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = banner.title,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (banner.subtitle.isNotEmpty()) {
                                                    Text(
                                                        text = banner.subtitle,
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontSize = 12.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Blue_text),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Thanh Lý Nhanh - Mua Bán Tiết Kiệm",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hàng ngàn món đồ cũ giá hời đang chờ bạn",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Button(
                                        onClick = { navController.navigate("category_screen") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Coral_Orange),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Đăng bán", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Danh mục",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            item {
                                CategoryItemImproved(
                                    title = "Tất cả",
                                    imageRes = R.drawable.ic_all,
                                    isSelected = selectedCategory == null,
                                    onClick = {
                                        selectedCategory = null
                                    }
                                )
                            }
                            item {
                                CategoryItemImproved(
                                    title = "Đồ điện tử",
                                    imageRes = R.drawable.ic_phone,
                                    isSelected = selectedCategory == "Thiết bị điện tử",
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == "Thiết bị điện tử") null else "Thiết bị điện tử"
                                    }
                                )
                            }
                            item {
                                CategoryItemImproved(
                                    title = "Xe máy",
                                    imageRes = R.drawable.ic_xemay,
                                    isSelected = selectedCategory == "Xe cộ",
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == "Xe cộ") null else "Xe cộ"
                                    }
                                )
                            }
                            item {
                                CategoryItemImproved(
                                    title = "Thời trang",
                                    imageRes = R.drawable.ic_aoo,
                                    isSelected = selectedCategory == "Quần áo",
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == "Quần áo") null else "Quần áo"
                                    }
                                )
                            }
                            item {
                                CategoryItemImproved(
                                    title = "Đồ gia dụng",
                                    imageRes = R.drawable.ic_noicom,
                                    isSelected = selectedCategory == "Đồ gia dụng",
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == "Đồ gia dụng") null else "Đồ gia dụng"
                                    }
                                )
                            }

                            // Dynamic categories from Firebase
                            val standardNames = listOf("Thiết bị điện tử", "Xe cộ", "Quần áo", "Đồ gia dụng", "Khác")
                            val extraCats = dynamicCategoryList.filter { it.name.isNotBlank() && !standardNames.contains(it.name) }
                            items(extraCats) { dynamicCat ->
                                CategoryItemImproved(
                                    title = dynamicCat.name,
                                    imageRes = R.drawable.ic_all,
                                    isSelected = selectedCategory == dynamicCat.name,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == dynamicCat.name) null else dynamicCat.name
                                    }
                                )
                            }

                            item {
                                CategoryItemImproved(
                                    title = "Khác",
                                    imageRes = R.drawable.ic_condit,
                                    isSelected = selectedCategory == "Khác",
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == "Khác") null else "Khác"
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                // Danh sách sản phẩm
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Blue_text)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang tải sản phẩm...",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Chia 2 cột
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(nestedScrollConnection)
                    ) {
                        items(sortedProducts) { product ->
                            ProductItem(
                                product = product,
                                navController = navController,
                                toggleProductVisibility = {
                                    productViewModel.toggleProductVisibility(
                                        product.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CategoryItemImproved(
    title: String,
    imageRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = if (isSelected) Blue_text.copy(alpha = 0.1f) else Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Blue_text else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) Blue_text else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
fun ProductItem(
    product: Product,
    onContactClick: () -> Unit = {},
    navController: NavController,
    toggleProductVisibility: () -> Unit
) {

    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favoriteIds = favoriteViewModel.favoriteProductIds.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            navController.navigate("product_detail/${product.id}")
        },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Hình ảnh sản phẩm
                AsyncImage(
                    model = product.imageUrl.replace("http://", "https://"),
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.placeholders_product),
                    error = painterResource(id = R.drawable.error)
                )

                // Trạng thái sản phẩm
                product.status.let { status ->
                    val statusColor = when (status) {
                        "sold" -> Color.Red
                        "available" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                    val statusText = when (status) {
                        "sold" -> "Đã bán"
                        "available" -> "Còn hàng"
                        else -> "Đã ẩn"
                    }
                    // Badge trạng thái
                    if (product.status == "sold") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                        Text(
                            text = "Đã bán",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(
                                    color = Blue_text.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Nút yêu thích
                IconButton(
                    onClick = { favoriteViewModel.toggleFavorite(product.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .padding(0.dp)
                        .offset((-10).dp, 10.dp)
                        .background(
                            color = Blue_text.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (favoriteIds.value.contains(product.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (favoriteIds.value.contains(product.id)) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.productName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(product.price.toDouble())} VNĐ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Coral_Orange
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.address,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
fun formatPrice(price: String): String {
    return try {
        val number = price.toDouble()
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        "${formatter.format(number)}₫"
    } catch (e: NumberFormatException) {
        price // Trả nguyên chuỗi nếu không phải số, ví dụ "Thỏa thuận"
    }
}
