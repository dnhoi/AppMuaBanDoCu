package com.example.appmuabandocu.feature_profile

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.appmuabandocu.R
import com.example.appmuabandocu.component.FeatureInDevelopmentDialog
import com.example.appmuabandocu.utils.formatRelativeTime
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.repository.ProductRepository
import com.example.appmuabandocu.repository.UserRepository
import com.example.appmuabandocu.viewmodel.ProfileUserViewModel
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.feature_home.formatPrice
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.viewmodel.FavoriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUserScreen(
    navController: NavController,
    userId: String
) {
    // Log userId để debug
    Log.d("ProfileUserScreen", "ProfileUserScreen được gọi với userId: '$userId'")

    // Get context safely
    val context = LocalContext.current

    // Create dependencies
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

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
        viewModel.loadUserProducts(userId)
    }

    val displayProducts = productsState.filter { it.status == "available" || it.status == "sold" }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    // Dialog thông báo tính năng đang phát triển
    var showFeatureDialog by remember { mutableStateOf(false) }
    var featureName by remember { mutableStateOf("") }

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
                        "Thông tin người bán",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                        featureName = "Chia sẻ thông tin người bán"
                        showFeatureDialog = true
                    },
                        ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ"
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Thêm"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang tải thông tin ...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                errorMessage?.let { error ->
                    ErrorView(
                        errorMessage = error,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.loadUserProfile(userId)
                            viewModel.loadUserProducts(userId)
                        }
                    )
                } ?: run {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            UserInfoSection(
                                user = userState,
                                isFollowing = isFollowing,
                                navController = navController
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sản phẩm đã đăng (${displayProducts.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            if (displayProducts.isEmpty()) {
                                EmptyProductsView()
                            } else {
                                ProductGrid(products = displayProducts, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thá»­ láº¡i")
        }
    }
}

val isFollowing = false
@Composable
fun UserInfoSection(
    user: User?,
    isFollowing: Boolean,
    navController: NavController? = null
) {
    if (user == null) return

    // Dialog thông báo tính năng đang phát triển
    var showFeatureDialog by remember { mutableStateOf(false) }
    var featureName by remember { mutableStateOf("") }

    if (showFeatureDialog) {
        FeatureInDevelopmentDialog(
            onDismiss = { showFeatureDialog = false },
            featureName = featureName
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.avatarUrl.replace("http://", "https://"))
                            .crossfade(true)
                            .build(),
                        contentDescription = user.fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.fullName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Online indicator
//                if (user.isOnline == true) {
//                    Box(
//                        modifier = Modifier
//                            .size(20.dp)
//                            .align(Alignment.BottomEnd)
//                            .offset(x = (-2).dp, y = (-2).dp)
//                            .clip(CircleShape)
//                            .background(Color.Green)
//                            .border(
//                                width = 2.dp,
//                                color = MaterialTheme.colorScheme.surface,
//                                shape = CircleShape
//                            )
//                    )
//                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

//                    if (user.isVerified == true) {
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Icon(
//                            imageVector = Icons.Default.Verified,
//                            contentDescription = "ÄÃ£ xÃ¡c thá»±c",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

//                Text(
//                    text = if (user.lastActive.isNotEmpty())
//                        "Hoáº¡t Ä‘á»™ng ${user.lastActive}" else
//                        "Hoáº¡t Ä‘á»™ng gáº§n Ä‘Ã¢y",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }
        }

        // Contact info
        Spacer(modifier = Modifier.height(16.dp))

        // Address
        val fullAddress = buildString {
            user.ward?.let {
                append(it)
            }
            user.district?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            user.province?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        }

        if (fullAddress.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = fullAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Action buttons
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Follow button
            Button(
                onClick = {
                    featureName = "Theo dõi"
                    showFeatureDialog = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing)
                        MaterialTheme.colorScheme.surfaceVariant else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isFollowing)
                        MaterialTheme.colorScheme.onSurfaceVariant else
                        MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isFollowing)
                        Icons.Default.PersonRemove else
                        Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFollowing) "Đang theo dõi" else "Theo dõi")
            }

            // Chat button
            OutlinedButton(
                onClick = {
                    if (navController != null && user.uid.isNotEmpty()) {
                        navController.navigate(
                            Screen.Chat.createRoute(
                                receiverId = user.uid,
                                receiverName = user.fullName
                            )
                        )
                    } else {
                        featureName = "Nhắn tin với người bán"
                        showFeatureDialog = true
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nhắn tin")
            }
        }

        // Reviews button
        OutlinedButton(
            onClick = {
                if (navController != null && user.uid.isNotEmpty()) {
                    navController.navigate(
                        Screen.Reviews.createRoute(
                            userId = user.uid,
                            userName = user.fullName
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF57F17)),
            border = BorderStroke(1.dp, Color(0xFFFFB300))
        ) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Xem đánh giá & uy tín người bán (5 sao)", fontWeight = FontWeight.SemiBold)
        }
    }
}
@Composable
fun EmptyProductsView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Không có sản phẩm nào được đăng",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun UserInfoSection1(user: User?) {
    if (user == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            if (user.avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = user.avatarUrl.replace("http://", "https://"),
                    contentDescription = user.fullName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Image(
                    painter = rememberAsyncImagePainter(user.avatarUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.fullName.firstOrNull()?.toString() ?: "?",
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = user.fullName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))


        Spacer(modifier = Modifier.height(8.dp))

        // Address
        val fullAddress = buildString {
            user.ward?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            user.district?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            user.province?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Address",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (fullAddress.isNotEmpty()) fullAddress else "Không có địa chỉ",
                fontSize = 14.sp,
                color = if (fullAddress.isNotEmpty()) Color.Black else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductGrid(products: List<Product>, navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((((products.size + 1) / 2) * 240).dp.coerceAtMost(480.dp))
    ) {
        items(products) { product ->
            ProductCard(product, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, navController: NavController) {

    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favoriteIds = favoriteViewModel.favoriteProductIds.collectAsState()

    Card(
        onClick = {
            navController.navigate("product_detail/${product.id}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl.replace("http://", "https://"))
                        .crossfade(true)
                        .build(),
                    contentDescription = product.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.status == "sold") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp)
                    ) {
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
                // Favorite button
                
            }

            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = product.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
