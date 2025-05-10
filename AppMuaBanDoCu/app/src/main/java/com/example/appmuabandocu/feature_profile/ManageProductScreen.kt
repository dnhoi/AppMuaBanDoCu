package com.example.appmuabandocu.feature_profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.viewmodel.ManageProductViewModel
import com.example.appmuabandocu.R
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.ui.theme.Gray_Hidden
import com.example.appmuabandocu.ui.theme.Green_Available
import com.example.appmuabandocu.ui.theme.Red_Sold
import com.example.appmuabandocu.ui.theme.orange
import com.example.appmuabandocu.ui.theme.Coral_Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(viewModel: ManageProductViewModel, navController: NavController) {
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value
    val message = viewModel.message.collectAsState().value
    val productList = viewModel.productList
    val productToDelete = viewModel.productToDelete.value

    // State cho dialog cập nhật trạng thái
    var showStatusDialog by remember { mutableStateOf(false) }
    var productToUpdateStatus by remember { mutableStateOf<Product?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background_Light
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Bài viết của tôi",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                modifier = Modifier.size(35.dp),
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Blue_text
                    )
                )
            },
            containerColor = Background_Light,
            floatingActionButton = {
                if (!isLoading && productList.isEmpty()) {
                    FloatingActionButton(
                        onClick = { /* Điều hướng đến màn hình thêm sản phẩm */ },
                        containerColor = Blue_text,
                        contentColor = Color.Red
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Thêm sản phẩm mới"
                        )
                    }
                }
            },

        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nội dung
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue_text)
                    }
                } else {
                    // Thông báo lỗi
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Thông báo thành công
                    AnimatedVisibility(
                        visible = message.isNotEmpty(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        if (message.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8F5E9)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = message,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Trạng thái trống
                    if (productList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_noicom),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Bạn chưa đăng sản phẩm nào.",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { /* Điều hướng đến màn hình thêm sản phẩm */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Blue_text
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Đăng sản phẩm mới", color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Danh sách sản phẩm
                        val context = LocalContext.current
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(productList.size) { index ->
                                EnhancedProductItem(
                                    product = productList[index],
                                    onProductClick = { navController.navigate("product_detail/${it.id}") },
                                    onDeleteClick = { viewModel.confirmDelete(it) },
                                    onStatusUpdateClick = {
                                        productToUpdateStatus = it
                                        showStatusDialog = true
                                    },
                                    onBumpClick = { product ->
                                        viewModel.bumpProduct(product) { success, msg ->
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                            // Thêm padding ở cuối
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }

                if (productToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.cancelDelete() },
                        title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
                        text = { Text("Bạn có chắc muốn xóa sản phẩm '${productToDelete.productName}' không?") },
                        confirmButton = {
                            TextButton(onClick = { viewModel.performDelete() }) {
                                Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.cancelDelete() }) {
                                Text("Hủy")
                            }
                        }
                    )
                }

                if (showStatusDialog && productToUpdateStatus != null) {
                    StatusUpdateDialog(
                        product = productToUpdateStatus!!,
                        onDismiss = { showStatusDialog = false },
                        onStatusUpdate = { product, newStatus ->
                            viewModel.updateProductStatus(product, newStatus)
                            showStatusDialog = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductItem(
    product: Product,
    onProductClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onStatusUpdateClick: (Product) -> Unit,
    onBumpClick: (Product) -> Unit
) {
    val status = product.status
    val now = remember { System.currentTimeMillis() }
    val oneDayMillis = 24 * 60 * 60 * 1000L
    val canBump = product.lastBumpedAt == 0L || (now - product.lastBumpedAt) >= oneDayMillis
    val remainingMillis = if (!canBump) (oneDayMillis - (now - product.lastBumpedAt)) else 0L
    val hoursRemaining = remainingMillis / (60 * 60 * 1000L)
    val minutesRemaining = (remainingMillis % (60 * 60 * 1000L)) / (60 * 1000L)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = { onProductClick(product) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Badge trạng thái
                val badgeColor = when(status) {
                    "sold" -> Red_Sold
                    "available" -> Green_Available
                    else -> Gray_Hidden
                }

                val badgeText = when(status) {
                    "sold" -> "Đã bán"
                    "available" -> "Còn hàng"
                    else -> "Đã ẩn"
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Nội dung sản phẩm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hình ảnh sản phẩm
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEEEEE))
                    ) {
                        AsyncImage(
                            model = product.imageUrl.replace("http://", "https://"),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_noicom),
                            error = painterResource(id = R.drawable.ic_condit)
                        )
                    }

                    // Chi tiết sản phẩm
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = product.productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${product.price} VND",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Coral_Orange,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Địa chỉ: ${product.address}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Nút hành động
            HorizontalDivider(color = Color(0xFFEEEEEE))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Đẩy tin (Bump Post)
                TextButton(
                    onClick = { onBumpClick(product) },
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Đẩy tin lên đầu",
                        tint = if (canBump) Coral_Orange else Color(0xFF9E9E9E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (canBump) "Đẩy tin" else "${hoursRemaining}h${minutesRemaining}m",
                        color = if (canBump) Coral_Orange else Color(0xFF9E9E9E),
                        fontWeight = if (canBump) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .align(Alignment.CenterVertically),
                    color = Color(0xFFEEEEEE)
                )

                TextButton(
                    onClick = { onStatusUpdateClick(product) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Cập nhật trạng thái",
                        tint = Blue_text,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "Cập nhật",
                        color = Blue_text,
                        fontSize = 12.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .align(Alignment.CenterVertically),
                    color = Color(0xFFEEEEEE)
                )

                TextButton(
                    onClick = { onDeleteClick(product) },
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa sản phẩm",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "Xóa",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusUpdateDialog(
    product: Product,
    onDismiss: () -> Unit,
    onStatusUpdate: (Product, String) -> Unit
) {
    val currentStatus = product.status
    var selectedStatus by remember { mutableStateOf(currentStatus) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cập nhật trạng thái",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sản phẩm: ${product.productName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Các nút radio cho trạng thái
                Column(modifier = Modifier.fillMaxWidth()) {
                    StatusRadioButton(
                        text = "Còn hàng",
                        selected = selectedStatus == "available",
                        color = Green_Available
                    ) {
                        selectedStatus = "available"
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusRadioButton(
                        text = "Đã bán",
                        selected = selectedStatus == "sold",
                        color = Red_Sold
                    ) {
                        selectedStatus = "sold"
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusRadioButton(
                        text = "Ẩn sản phẩm",
                        selected = selectedStatus == "hidden",
                        color = Gray_Hidden
                    ) {
                        selectedStatus = "hidden"
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút hành động
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onStatusUpdate(product, selectedStatus) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue_text
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cập nhật", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusRadioButton(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = color
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 16.sp,
            color = if (selected) color else Color.Black
        )
    }
}