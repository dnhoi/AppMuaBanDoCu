package com.example.appmuabandocu.feature_mxh

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.R
import com.example.appmuabandocu.utils.formatRelativeTime
import com.example.appmuabandocu.core.navigation.BottomNavBar
import com.example.appmuabandocu.viewmodel.ProfileUserViewModel
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.repository.ProductRepository
import com.example.appmuabandocu.repository.UserRepository
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.ui.theme.orange
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.example.appmuabandocu.viewmodel.FavoriteViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.toString


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MxhScreen(
   navController: NavController,
   productViewModel: ProductViewModel = viewModel(),
){
    val context = LocalContext.current
    val products = productViewModel.loadVisibleProducts()
    val searchResults by productViewModel.searchResults.collectAsState()

    var query by remember { mutableStateOf("") }
    val isRefreshing by productViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
       refreshing = isRefreshing,
       onRefresh = { productViewModel.refreshProducts() }
    )
    val displayProducts = if (query.isNotBlank()) {
        searchResults.filter { it.status == "available" || it.status == "sold" }
    } else {
        products
    }

    val sortedSearchResults = displayProducts.sortedByDescending { it.timestamp }


   // Hiệu ứng scroll
   val scrollState = rememberLazyListState()
   val isScrolled = scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0

   // Log để kiểm tra danh sách sản phẩm
   LaunchedEffect(products) {
       Log.d("mxhScreen", "Sản phẩm: ${products.size} sản phẩm")
   }
   Surface(
       modifier = Modifier.fillMaxSize(),
       color = Background_Light
   ) {
       Scaffold(
           topBar = {
               Surface(
                   modifier = Modifier
                       .fillMaxWidth()
                       .shadow(
                           elevation = if (isScrolled) 4.dp else 0.dp,
                           ambientColor = Color.Black.copy(alpha = 0.1f),
                           spotColor = Color.Black.copy(alpha = 0.1f)
                       ),
                   color = Blue_text
               ) {
                   Column(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(horizontal = 16.dp, vertical = 12.dp)
                   ) {
                       Spacer(modifier = Modifier.height(40.dp))
                       // Tiêu đề
                       Text(
                           text = "Thanh lý nhanh",
                           fontSize = 22.sp,
                           fontWeight = FontWeight.Bold,
                           color = Color.White
                       )

                       Spacer(modifier = Modifier.height(12.dp))

                       // Thanh tìm kiếm
                       Card(
                           modifier = Modifier
                               .fillMaxWidth()
                               .height(56.dp),
                           shape = RoundedCornerShape(12.dp),
                           colors = CardDefaults.cardColors(
                               containerColor = Color.White
                           ),
                           elevation = CardDefaults.cardElevation(
                               defaultElevation = 2.dp
                           )
                       ) {
                           Row(
                               modifier = Modifier
                                   .fillMaxSize()
                                   .padding(horizontal = 12.dp),
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               Icon(
                                   imageVector = Icons.Default.Search,
                                   contentDescription = "Search",
                                   tint = Color.Gray,
                                   modifier = Modifier.size(20.dp)
                               )

                               Spacer(modifier = Modifier.width(8.dp))

                               BasicTextField(
                                   value = query,
                                   onValueChange = {
                                       query = it
                                       productViewModel.searchProducts(it)
                                   },
                                   modifier = Modifier
                                       .weight(1f),
                                   textStyle = TextStyle(
                                       fontSize = 16.sp,
                                       color = Color.Black
                                   ),
                                   singleLine = true,
                                   cursorBrush = SolidColor(Blue_text),
                                   decorationBox = { innerTextField ->
                                       Box {
                                           if (query.isEmpty()) {
                                               Text(
                                                   "Tìm kiếm sản phẩm...",
                                                   color = Color.Gray
                                               )
                                           }
                                           innerTextField()
                                       }
                                   }
                               )

                               if (query.isNotEmpty()) {
                                   IconButton(onClick = {
                                       query = ""
                                       productViewModel.searchProducts("")
                                   }) {
                                       Icon(
                                           imageVector = Icons.Default.Close,
                                           contentDescription = "Xóa",
                                           tint = Color.Gray,
                                           modifier = Modifier.size(20.dp)
                                       )
                                   }
                               }
                           }
                       }
                   }
               }
           },
           bottomBar = { BottomNavBar(navController = navController) },
           containerColor = Background_Light
       ) { paddingValues ->
           Box(
               modifier = Modifier
                   .fillMaxSize()
                   .padding(paddingValues)
                   .pullRefresh(pullRefreshState)
           ) {
               if (sortedSearchResults.isEmpty() && query.isEmpty() && !isRefreshing) {
                   // Trạng thái trống
                   EmptyProductState()
               } else {
                   // Danh sách sản phẩm
                   LazyColumn(
                       state = scrollState,
                       contentPadding = PaddingValues(bottom = 16.dp),
                       verticalArrangement = Arrangement.spacedBy(8.dp)
                   ) {
                       item {
                           Spacer(modifier = Modifier.height(8.dp))
                       }

                       if (query.isNotEmpty()) {
                           item {
                               Text(
                                   text = "Kết quả tìm kiếm: ${sortedSearchResults.size} sản phẩm",
                                   modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                   fontSize = 14.sp,
                                   fontWeight = FontWeight.Medium,
                                   color = Color.Gray
                               )
                           }
                       }

                       items(
                           items = sortedSearchResults,
                           key = { it.id }
                       ) { product ->
                           EnhancedProductItemMXH(
                               product = product,
                               navController = navController,
                           )
                       }

                       item {
                           Spacer(modifier = Modifier.height(80.dp))
                       }
                   }

                   // Pull-to-refresh indicator
                   PullRefreshIndicator(
                       refreshing = isRefreshing,
                       state = pullRefreshState,
                       modifier = Modifier.align(Alignment.TopCenter),
                       backgroundColor = orange,
                       contentColor = Color.White
                   )
               }
           }
       }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductItemMXH(
   product: Product,
   navController: NavController,
) {
   val context = LocalContext.current
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

    LaunchedEffect(product.userId) {
        product.userId.let { userId ->
            loadingUserData = true
            viewModel.loadUserProfile(userId)
            viewModel.loadUserProducts(userId)
            // loadingUserData should be set to false when loading completes
            // This should ideally happen in the ViewModel after data is loaded
        }
    }
    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favoriteIds = favoriteViewModel.favoriteProductIds.collectAsState()

    val formattedTime = remember(product.timestamp) {
         formatRelativeTime(product.timestamp)
    }
   // Trạng thái menu
   var expanded by remember { mutableStateOf(false) }

   val scale by animateFloatAsState(
       targetValue = if (favoriteIds.value.contains(product.id)) 1.2f else 1f,
       animationSpec = spring(
           dampingRatio = Spring.DampingRatioMediumBouncy,
           stiffness = Spring.StiffnessLow
       ),
       label = "favorite_scale"
   )

   Card(
       modifier = Modifier
           .fillMaxWidth()
           .padding(horizontal = 16.dp, vertical = 4.dp),
       shape = RoundedCornerShape(16.dp),
       colors = CardDefaults.cardColors(
           containerColor = Color.White
       ),
       elevation = CardDefaults.cardElevation(
           defaultElevation = 2.dp
       ),
       onClick = { navController.navigate("product_detail/${product.id}") }
   ) {
       Column(
           modifier = Modifier.padding(16.dp)
       ) {
           // Header - Thông tin người đăng
           Row(
               modifier = Modifier.fillMaxWidth(),
               verticalAlignment = Alignment.CenterVertically
           ) {
               // Avatar người đăng
               Card(
                   shape = CircleShape,
                   elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
               ) {
                   AsyncImage(
                       model = userState?.avatarUrl?.replace("http://", "https://"),
                       contentDescription = "Avatar",
                       modifier = Modifier.size(40.dp),
                       contentScale = ContentScale.Crop,
                       error = rememberVectorPainter(Icons.Default.Person)
                   )
               }

               Spacer(modifier = Modifier.width(12.dp))

               // Thông tin người đăng và thời gian
               Column(
                   modifier = Modifier.weight(1f)
               ) {
                   Text(
                       buildAnnotatedString {
                           withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                               append(userState?.fullName ?: "")
                           }
                           withStyle(style = SpanStyle(color = Color.DarkGray)) {
                               append(" đã đăng một mặt hàng")
                           }
                       },
                       fontSize = 14.sp
                   )

                   Spacer(modifier = Modifier.height(2.dp))

                   Row(
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Icon(
                           imageVector = Icons.Default.AccessTime,
                           contentDescription = null,
                           modifier = Modifier.size(12.dp),
                           tint = Color.Gray
                       )
                       Spacer(modifier = Modifier.width(4.dp))
                       Text(
                           text = formattedTime,
                           fontSize = 12.sp,
                           color = Color.Gray
                       )

                       Spacer(modifier = Modifier.width(8.dp))

                       Icon(
                           imageVector = Icons.Default.LocationOn,
                           contentDescription = null,
                           modifier = Modifier.size(12.dp),
                           tint = Color.Gray
                       )
                       Spacer(modifier = Modifier.width(4.dp))
                       Text(
                           text = product.provinceFB,
                           fontSize = 12.sp,
                           color = Color.Gray
                       )
                   }
               }

               // Menu
               Box {
                   IconButton(onClick = { expanded = true }) {
                       Icon(
                           imageVector = Icons.Default.MoreVert,
                           contentDescription = "More",
                           tint = Color.Gray
                       )
                   }

                   DropdownMenu(
                       expanded = expanded,
                       onDismissRequest = { expanded = false }
                   ) {
                       DropdownMenuItem(
                           text = { Text("Báo cáo bài viết") },
                           onClick = {
                               expanded = false
                               Toast.makeText(context, "Đã báo cáo bài viết", Toast.LENGTH_SHORT).show()
                           },
                           leadingIcon = {
                               Icon(
                                   imageVector = Icons.Default.Flag,
                                   contentDescription = null
                               )
                           }
                       )
                       DropdownMenuItem(
                           text = { Text("Ẩn bài viết") },
                           onClick = {
                               expanded = false
                           },
                           leadingIcon = {
                               Icon(
                                   imageVector = Icons.Default.VisibilityOff,
                                   contentDescription = null
                               )
                           }
                       )
                   }
               }
           }

           Spacer(modifier = Modifier.height(12.dp))

           // Hình ảnh sản phẩm
           Card(
               shape = RoundedCornerShape(12.dp),
               elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
           ) {
               AsyncImage(
                   model = product.imageUrl.replace("http://", "https://"),
                   contentDescription = "Hình sản phẩm",
                   modifier = Modifier
                       .fillMaxWidth()
                       .height(200.dp),
                   contentScale = ContentScale.Crop,
                   placeholder = painterResource(id = R.drawable.placeholders_product),
                   error = painterResource(id = R.drawable.error)
               )
           }

           Spacer(modifier = Modifier.height(12.dp))

           // Thông tin sản phẩm
           Text(
               text = product.productName,
               fontWeight = FontWeight.Bold,
               fontSize = 18.sp,
               color = Color.Black
           )

           Spacer(modifier = Modifier.height(4.dp))

           Text(
               text = formatPrice(product.price.toString()),
               color = Blue_text,
               fontSize = 16.sp,
               fontWeight = FontWeight.Bold
           )

           Spacer(modifier = Modifier.height(4.dp))

           // Mô tả sản phẩm (nếu có)
           product.details.let {
               if (it.isNotEmpty()) {
                   Text(
                       text = it,
                       fontSize = 14.sp,
                       color = Color.DarkGray,
                       maxLines = 2,
                       overflow = TextOverflow.Ellipsis
                   )
                   Spacer(modifier = Modifier.height(8.dp))
               }
           }

           // Trạng thái sản phẩm
           product.status.let { status ->
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
                   verticalAlignment = Alignment.CenterVertically,
                   modifier = Modifier.padding(vertical = 4.dp)
               ) {
                   Box(
                       modifier = Modifier
                           .size(8.dp)
                           .background(statusColor, CircleShape)
                   )
                   Spacer(modifier = Modifier.width(4.dp))
                   Text(
                       text = statusText,
                       fontSize = 14.sp,
                       color = statusColor,
                       fontWeight = FontWeight.Medium
                   )
               }
           }

           Spacer(modifier = Modifier.height(12.dp))

           // Các nút hành động
           Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.SpaceBetween
           ) {
               // Nút liên hệ
               Button(
                   onClick = {
                       val intent = Intent(Intent.ACTION_DIAL).apply {
                           data = "tel:${product.numberUser}".toUri()
                       }
                       context.startActivity(intent)
                   },
                   modifier = Modifier.weight(1f),
                   colors = ButtonDefaults.buttonColors(
                       containerColor = Blue_text
                   ),
                   shape = RoundedCornerShape(8.dp)
               ) {
                   Icon(
                       imageVector = Icons.Default.Phone,
                       contentDescription = "Liên hệ",
                       modifier = Modifier.size(18.dp)
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   Text(
                       text = "Liên hệ ngay",
                       fontWeight = FontWeight.Medium
                   )
               }

               Spacer(modifier = Modifier.width(12.dp))

               // Nút yêu thích
               IconButton(
                   onClick = { favoriteViewModel.toggleFavorite(product.id) },
                   modifier = Modifier
                       .size(48.dp)
                       .background(
                           color = if (favoriteIds.value.contains(product.id)) orange.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
                           shape = CircleShape
                       )
                       .scale(scale)
               ) {
                   Icon(
                       imageVector = if (favoriteIds.value.contains(product.id)) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                       contentDescription = "Yêu thích",
                       tint = if (favoriteIds.value.contains(product.id)) orange else Color.Gray,
                       modifier = Modifier.size(24.dp)
                   )
               }
           }
       }
   }
}

@Composable
fun EmptyProductState() {
   Box(
       modifier = Modifier
           .fillMaxSize()
           .padding(16.dp),
       contentAlignment = Alignment.Center
   ) {
       Column(
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center
       ) {
           Image(
               painter = painterResource(id = R.drawable.ic_empty_task),
               contentDescription = "Không có sản phẩm",
               modifier = Modifier
                   .size(160.dp)
                   .padding(bottom = 24.dp)
           )

           Text(
               text = "Chưa có sản phẩm nào",
               fontSize = 20.sp,
               fontWeight = FontWeight.Bold,
               color = Color.DarkGray
           )

           Spacer(modifier = Modifier.height(8.dp))

           Text(
               text = "Hãy thêm sản phẩm mới hoặc kéo xuống để làm mới",
               fontSize = 16.sp,
               color = Color.Gray,
               textAlign = TextAlign.Center
           )
       }
   }
}

@Composable
fun ProductItemMXH(
   product1: Product,
   navController: NavController,
   toggleProductVisibility: () -> Unit
) {
   val formattedTime = remember(product1.timestamp) {
       val date = Date(product1.timestamp)
       val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
       formatter.format(date)
   }
   val phoneNumber = product1.numberUser
   val context = LocalContext.current


   Card(
       modifier = Modifier
           .fillMaxWidth()
           .background(Color.White)
           .padding(bottom = 8.dp),
       colors = CardDefaults.cardColors(containerColor = Color.White),
       elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
       onClick = { navController.navigate("product_detail/${product1.id}") },
   ) {
       Divider(
           color = Blue_text,
           thickness = 1.dp,
           modifier = Modifier.padding(vertical = 8.dp)
       )
       Column(modifier = Modifier.padding(12.dp)) {
           Row(verticalAlignment = Alignment.CenterVertically) {
               AsyncImage(
                   model = product1.userAvatar.replace("http://", "https://"),
                   contentDescription = null,
                   contentScale = ContentScale.Crop,
                   modifier = Modifier
                       .size(40.dp)
                       .clip(CircleShape),
               )

               Spacer(modifier = Modifier.width(8.dp))

               Column {
                   Text(
                       buildAnnotatedString {
                           withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                               append(product1.userName)
                           }
                           withStyle(style = SpanStyle(color = Color.Black)) {
                               append(" đã đăng một mặt hàng")
                           }
                       },
                       fontSize = 14.sp
                   )
                   Row {
                       Text(text = formattedTime, fontSize = 12.sp, color = Color.Gray)
                       Spacer(modifier = Modifier.width(4.dp))
                       Text(text = product1.provinceFB, fontSize = 12.sp, color = Color.Gray)
                   }
               }
               Spacer(modifier = Modifier.weight(1f))


               Icon(
                   imageVector = Icons.Default.MoreVert,
                   contentDescription = "More"
               )
           }


           Spacer(modifier = Modifier.height(8.dp))


           // Product Image
           AsyncImage(
               model = product1.imageUrl.replace("http://", "https://"),
               contentDescription = null,
               contentScale = ContentScale.Fit,
               modifier = Modifier
                   .fillMaxWidth()
                   .wrapContentHeight()
                   .clip(RoundedCornerShape(8.dp)),
               placeholder = painterResource(id = R.drawable.placeholders_product), // Thêm ảnh placeholder
               error = painterResource(id = R.drawable.error), // Thêm ảnh khi có lỗi
           )


           Spacer(modifier = Modifier.height(8.dp))


           // Product Info
           Text(text = "Tên sản phẩm: ${product1.productName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
           Text(text = "Giá bán: ${formatPrice(product1.price.toString())}", color = Color.Black, fontSize = 15.sp)


           Spacer(modifier = Modifier.height(8.dp))


           // Contact Button
           TextButton(
               onClick = {  val intent = Intent(Intent.ACTION_DIAL).apply {
                   data = "tel:$phoneNumber".toUri()
               }
                   context.startActivity(intent) },
               modifier = Modifier
                   .fillMaxWidth()
                   .height(45.dp),
           ) {
               Icon(
                   imageVector = Icons.Default.Phone,
                   contentDescription = null,
                   tint = Blue_text
               )
               Spacer(modifier = Modifier.width(8.dp))
               Text(
                   text = "Liên hệ ngay",
                   color = Blue_text,
                   fontWeight = FontWeight.Bold,
                   fontSize = 16.sp
               )
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

