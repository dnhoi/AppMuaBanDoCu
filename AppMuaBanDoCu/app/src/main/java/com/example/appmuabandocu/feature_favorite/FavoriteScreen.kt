package com.example.appmuabandocu.feature_favorite

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.R
import com.example.appmuabandocu.core.navigation.BottomNavBar
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.core.ui.SplashRoleScreen
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.feature_home.formatPrice
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.ui.theme.orange
import com.example.appmuabandocu.ui.theme.Coral_Orange
import com.example.appmuabandocu.viewmodel.AuthViewModel
import com.example.appmuabandocu.viewmodel.FavoriteViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavController,
    viewModel: ProductViewModel,
    favoriteViewModel: FavoriteViewModel,
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    // Kiểm tra đăng nhập
    LaunchedEffect(key1 = user) {
        if (user == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Favorite.route) { inclusive = true }
            }
        }
    }

    // Trạng thái lọc danh mục
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Tất cả", "Điện tử", "Thời trang", "Đồ gia dụng", "Khác")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background_Light
    ) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp),
                    color = Blue_text
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Bài viết yêu thích",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            bottomBar = { BottomNavBar(navController = navController) },
            containerColor = Background_Light
        ) { paddingValues ->
            if (user != null) {
                // Chỉ lấy danh sách yêu thích khi người dùng đã đăng nhập
                val favoriteProductIds = favoriteViewModel.favoriteProductIds.collectAsState().value
                val allFavoriteProducts = remember(viewModel.productList, favoriteProductIds) {
                    viewModel.productList.filter { product ->
                        favoriteProductIds.contains(product.id)
                    }
                }

                // Lọc theo danh mục nếu có
                val favoriteProducts = remember(allFavoriteProducts, selectedCategory) {
                    if (selectedCategory == null || selectedCategory == "Tất cả") {
                        allFavoriteProducts
                    } else {
                        allFavoriteProducts.filter { it.category == selectedCategory }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Danh mục lọc
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category || (category == "Tất cả" && selectedCategory == null),
                                onClick = {
                                    selectedCategory = if (category == "Tất cả") null else category
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue_text,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false,
                                    borderColor = Blue_text.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // Nội dung chính
                    val isLoading = favoriteViewModel.isLoading.collectAsState(initial = true).value

                    if (isLoading) {
                        // Hiệu ứng loading với shimmer
                        ShimmerFavoriteList()
                    } else if (favoriteProducts.isEmpty()) {
                        // Trạng thái trống - chỉ hiển thị khi không loading
                        EmptyFavoriteState(navController)
                    } else {
                        // Danh sách sản phẩm yêu thích
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = favoriteProducts,
                                key = { it.id }
                            ) { product ->
                                EnhancedProductItem(
                                    product = product,
                                    viewModel = favoriteViewModel,
                                    navController = navController
                                )
                            }
                            // Thêm padding ở cuối
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductItem(
    product: Product,
    viewModel: FavoriteViewModel,
    navController: NavController
) {
    val favoriteIds = viewModel.favoriteProductIds.collectAsState()
    val isFavorite = favoriteIds.value.contains(product.id)

    // Animation cho nút yêu thích
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = {
            navController.navigate("product_detail/${product.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hình ảnh sản phẩm
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl.replace("http://", "https://"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.placeholders_product),
                    error = painterResource(id = R.drawable.error),
                )
            }

            // Thông tin sản phẩm
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Coral_Orange,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Hiển thị trạng thái sản phẩm nếu có
                product.status?.let { status ->
                    Spacer(modifier = Modifier.height(4.dp))
                    val statusColor = when(status) {
                        "sold" -> Color.Red
                        "available" -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                    val statusText = when(status) {
                        "sold" -> "Đã bán"
                        "available" -> "Còn hàng"
                        else -> "Đã ẩn"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Nút yêu thích
            IconButton(
                onClick = { viewModel.toggleFavorite(product.id) },
                modifier = Modifier
                    .scale(scale)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Yêu thích",
                    tint = if (isFavorite) Blue_text else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyFavoriteState(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hình ảnh minh họa
            Image(
                painter = painterResource(id = R.drawable.ic_empty_task),
                contentDescription = "Không có sản phẩm yêu thích",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 24.dp)
            )

            // Tiêu đề
            Text(
                text = "Chưa có sản phẩm yêu thích",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mô tả
            Text(
                text = "Hãy thêm sản phẩm yêu thích để xem ở đây",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút khám phá sản phẩm
            Button(
                onClick = { navController.navigate(Screen.Home.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue_text
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Khám phá sản phẩm",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ShimmerFavoriteList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        repeat(5) {
            ShimmerProductItem()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ShimmerProductItem() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shimmer cho hình ảnh
            Spacer(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                // Shimmer cho tiêu đề
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .padding(vertical = 2.dp)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shimmer cho giá
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .padding(vertical = 2.dp)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shimmer cho địa chỉ
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .padding(vertical = 2.dp)
                        .background(brush)
                )
            }

            // Shimmer cho nút yêu thích
            Spacer(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
        }
    }
}

// Hàm định dạng giá tiền
fun formatPrice(price: Double): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(price)} VNĐ"
}