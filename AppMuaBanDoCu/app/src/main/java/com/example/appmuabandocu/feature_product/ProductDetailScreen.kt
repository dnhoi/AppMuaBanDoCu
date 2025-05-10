package com.example.appmuabandocu.feature_product

import android.content.Intent
import android.widget.Toast
import com.example.appmuabandocu.viewmodel.AdminViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.appmuabandocu.feature_mxh.formatPrice
import com.example.appmuabandocu.viewmodel.FavoriteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.outlined.Error
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.DialogProperties
import coil.request.ImageRequest
import com.example.appmuabandocu.component.FeatureInDevelopmentDialog
import com.example.appmuabandocu.utils.formatRelativeTime
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.viewmodel.ProfileUserViewModel
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.repository.ProductRepository
import com.example.appmuabandocu.repository.UserRepository
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.orange
import com.example.appmuabandocu.ui.theme.Coral_Orange
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.plusAssign
import kotlin.compareTo
import kotlin.dec
import kotlin.div
import kotlin.inc
import kotlin.times
import kotlin.unaryMinus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
navController: NavController,
id: String,
viewModel: ProductViewModel = viewModel(),
favoriteViewModel: FavoriteViewModel = viewModel()
) {
    val product = viewModel.productList.find { it.id == id }
    val context = LocalContext.current
    val phoneNumber = product?.numberUser
    val favoriteIds = favoriteViewModel.favoriteProductIds.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    var currentImageIndex by remember { mutableIntStateOf(0) }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Lừa đảo / Không giao hàng") }
    var reportDetails by remember { mutableStateOf("") }

    val imageUrls = remember(product) {
        if (product != null) {
            listOf(product.imageUrl) + product.imageMota
        } else {
            emptyList()
        }
    }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val networkObserver = remember { NetworkConnectivityObserver(context) }

    // Create repositories
    val userRepository = remember { UserRepository(firestore, auth, networkObserver) }
    val productRepository = remember { ProductRepository(firestore, networkObserver) }

    val viewModel = remember {
        ProfileUserViewModel(userRepository, productRepository)
    }

    val userState by viewModel.user.collectAsState()
    val productsState by viewModel.userProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Thêm trạng thái để theo dõi việc tải dữ liệu người dùng
    var loadingUserData by remember { mutableStateOf(true) }

    LaunchedEffect(product?.userId) {
        if (product?.userId != null) {
            loadingUserData = true
            viewModel.loadUserProfile(product.userId.toString())
            viewModel.loadUserProducts(product.userId.toString())
            loadingUserData = false
        }
    }

    val displayProducts = productsState.filter { it.status == "available" || it.status == "sold" }


    // Dialog thông báo tính năng đang phát triển
    var showFeatureDialog by remember { mutableStateOf(false) }
    var featureName by remember { mutableStateOf("") }

    val formattedTime = remember(product?.timestamp) {
        product?.timestamp?.let { formatRelativeTime(it) }
    }

    if (showFeatureDialog) {
        FeatureInDevelopmentDialog(
            onDismiss = { showFeatureDialog = false },
            featureName = featureName
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
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (product != null) {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Xem món đồ cũ \"${product.productName}\" - Giá: ${product.price}đ trên Chợ Đồ Cũ!\nKhu vực: ${product.address.ifEmpty { "Việt Nam" }}\nNgười bán: ${product.userName}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ sản phẩm"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showReportDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Báo cáo vi phạm",
                            tint = Color(0xFFFF8A80)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (product != null) {
                                favoriteViewModel.toggleFavorite(product.id)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (product != null && favoriteIds.value.contains(product.id))
                                Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (product != null && favoriteIds.value.contains(product.id))
                                Color.Red else Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Không tìm thấy sản phẩm",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue_text
                        )
                    ) {
                        Text("Quay lại")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Phần hình ảnh sản phẩm
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        // Hiển thị hình ảnh chính
                        AsyncImage(
                            model = imageUrls.getOrNull(currentImageIndex)?.replace("http://", "https://"),
                            contentDescription = "Hình ảnh sản phẩm",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top=4.dp)
                                .clickable {
                                    if (imageUrls.isNotEmpty()) {
                                        selectedImageUrl = imageUrls[currentImageIndex].replace("http://", "https://")
                                        showDialog = true
                                    }
                                },
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.ic_noicom),
                            error = painterResource(id = R.drawable.ic_xemay)
                        )

                        // Hiển thị trạng thái sản phẩm nếu đã bán
                        if (product.status == "sold") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                            )
                            Text(
                                text = "Đã bán",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(
                                        color = Color.Red.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }

                        // Hiển thị chỉ số hình ảnh
                        if (imageUrls.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${currentImageIndex + 1}/${imageUrls.size}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Nút điều hướng hình ảnh
                        if (imageUrls.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentImageIndex > 0) {
                                            currentImageIndex--
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (currentImageIndex < imageUrls.size - 1) {
                                            currentImageIndex++
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Next",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Thêm dòng thumbnails
                    if (imageUrls.size > 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Background_Light)
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                itemsIndexed(imageUrls) { index, item ->
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = 2.dp,
                                                color = if (index == currentImageIndex) Blue_text else Color.LightGray,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                currentImageIndex = index
                                            }
                                    ) {
                                        AsyncImage(
                                            model = item.replace("http://", "https://"),
                                            contentDescription = "Ảnh nhỏ ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(id = R.drawable.ic_noicom),
                                            error = painterResource(id = R.drawable.ic_xemay)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Phần thông tin sản phẩm
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = formatPrice(product.price),
                                style = MaterialTheme.typography.titleLarge,
                                color = Coral_Orange,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Time",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "Đăng lúc: $formattedTime",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))



                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Thông tin chi tiết",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF565656)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            InfoRowImproved("Loại sản phẩm ", product.category)
                            InfoRowImproved("Tình trạng giá", if (product.negotiable) "Có thể thương lượng" else "Không trả giá")
                            InfoRowImproved("Giao hàng", if (product.freeShip) "Miễn phí ship" else "Không có freeship")

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = product.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Mô tả sản phẩm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF565656) // Màu xám đậm cho tiêu đề mô tả
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = product.details.ifEmpty { "Không có mô tả chi tiết" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                    }

                    // Phần thông tin người bán
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Thông tin người bán",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF565656)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Hiển thị loading khi đang tải thông tin người dùng
                            if (loadingUserData || isLoading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Blue_text,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đang tải thông tin người bán...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar người bán
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (userState?.avatarUrl?.isNotEmpty() == true) {
                                            AsyncImage(
                                                model = userState?.avatarUrl?.replace("http://", "https://"),
                                                contentDescription = "User Avatar",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "User",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = userState?.fullName ?: "người dùng",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Đã đăng ${displayProducts.size} sản phẩm",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            android.util.Log.d("ProductDetailScreen", "Navigating to user profile with userId: ${product.userId}")
                                            navController.navigate(Screen.ProfileUser.createRoute(product.userId))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowRight,
                                            contentDescription = "View Profile",
                                            tint = Blue_text
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Nút liên hệ
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:$phoneNumber".toUri()
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue_text
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Liên hệ ${product.numberUser}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Nút chat trực tiếp với người bán
                    OutlinedButton(
                        onClick = {
                            if (product.userId.isNotEmpty()) {
                                navController.navigate(
                                    Screen.Chat.createRoute(
                                        receiverId = product.userId,
                                        receiverName = product.userName,
                                        productId = product.id
                                    )
                                )
                            } else {
                                Toast.makeText(context, "Không xác định được người bán", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Blue_text),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue_text)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Nhắn tin với người bán", style = MaterialTheme.typography.titleMedium)
                    }

                    // Nút xem đánh giá uy tín người bán
                    OutlinedButton(
                        onClick = {
                            if (product.userId.isNotEmpty()) {
                                navController.navigate(
                                    Screen.Reviews.createRoute(
                                        userId = product.userId,
                                        userName = product.userName
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB300)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF57F17))
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Đánh giá", tint = Color(0xFFFFB300))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Xem đánh giá & uy tín người bán", style = MaterialTheme.typography.titleMedium)
                    }

                    // Nút báo cáo vi phạm
                    TextButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Report, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Báo cáo bài viết có dấu hiệu vi phạm", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Dialog xem ảnh toàn màn hình
            if (showDialog) {
                FullScreenImageDialogImproved(
                    imageUrl = selectedImageUrl,
                    onDismiss = { showDialog = false },
                    allImages = imageUrls // Truyền danh sách đầy đủ các ảnh
                )
            }

            // Dialog báo cáo vi phạm
            if (showReportDialog && product != null) {
                val reasons = listOf(
                    "Lừa đảo / Không giao hàng",
                    "Hàng giả / Hàng nhái / Hàng cấm",
                    "Sai mô tả / Ảnh không đúng thực tế",
                    "Thông tin liên hệ sai / Spam / Đồ đã bán",
                    "Lý do khác"
                )
                AlertDialog(
                    onDismissRequest = { showReportDialog = false },
                    title = {
                        Text(text = "Báo cáo bài viết vi phạm", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            Text(text = "Chọn lý do báo cáo:", fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            reasons.forEach { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { reportReason = r }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = reportReason == r,
                                        onClick = { reportReason = r },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5252))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = r, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reportDetails,
                                onValueChange = { reportDetails = it },
                                label = { Text("Mô tả chi tiết (tùy chọn)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                AdminViewModel.sendReport(
                                    productId = product.id,
                                    productName = product.productName,
                                    productImage = product.imageUrl.ifEmpty { product.imageMota.firstOrNull() ?: "" },
                                    sellerId = product.userId,
                                    sellerName = product.userName,
                                    reason = reportReason,
                                    details = reportDetails,
                                    onSuccess = {
                                        Toast.makeText(context, "Đã gửi báo cáo vi phạm tới Quản trị viên!", Toast.LENGTH_LONG).show()
                                        showReportDialog = false
                                        reportDetails = ""
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Lỗi gửi báo cáo: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Gửi báo cáo", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showReportDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoRowImproved(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
        )
    }
}

@Composable
fun FullScreenImageDialogImproved(
    imageUrl: String,
    onDismiss: () -> Unit,
    allImages: List<String> = listOf(imageUrl)
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var currentIndex by remember {
        mutableIntStateOf(allImages.indexOf(imageUrl).coerceAtLeast(0))
    }

    // Thêm biến offset cho hiệu ứng vuốt
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            if (scale <= 1.1f) {
                                if (dragOffsetX > swipeThreshold && currentIndex > 0) {
                                    currentIndex--
                                    scale = 1f
                                    offset = Offset.Zero
                                } else if (dragOffsetX < -swipeThreshold && currentIndex < allImages.size - 1) {
                                    currentIndex++
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = {
                            dragOffsetX = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (scale <= 1.1f) {
                                dragOffsetX += dragAmount.x
                            }
                        }
                    )
                }
        ) {
            // Main image với zoom và pan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (dragOffsetX == 0f) {  // Chỉ xử lý phóng to khi không đang vuốt
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                val maxX = (size.width * (scale - 1)) / 2
                                val maxY = (size.height * (scale - 1)) / 2
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                )
                            }
                        }
                    }
            ) {
                AsyncImage(
                    model = allImages.getOrNull(currentIndex)?.replace("http://", "https://") ?: imageUrl,
                    contentDescription = "Ảnh phóng to",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = R.drawable.ic_noicom),
                    error = painterResource(id = R.drawable.ic_xemay)
                )
            }

            // Thêm chỉ báo hướng dẫn swipe khi có nhiều ảnh
            if (allImages.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Vuốt để xem ảnh khác",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }

            // Nút đóng
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.White
                )
            }

            // Thông tin slide
            Text(
                text = "${currentIndex + 1}/${allImages.size}",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Nút điều hướng trái phải
            if (allImages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (currentIndex > 0) {
                                currentIndex--
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Ảnh trước",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentIndex < allImages.size - 1) {
                                currentIndex++
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Ảnh sau",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Thumbnails ở dưới
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(allImages) { index, item ->
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (index == currentIndex) Blue_text else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    currentIndex = index
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                        ) {
                            AsyncImage(
                                model = item.replace("http://", "https://"),
                                contentDescription = "Ảnh nhỏ ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_noicom),
                                error = painterResource(id = R.drawable.ic_xemay)
                            )

                            if (index == currentIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Blue_text.copy(alpha = 0.3f))
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}
