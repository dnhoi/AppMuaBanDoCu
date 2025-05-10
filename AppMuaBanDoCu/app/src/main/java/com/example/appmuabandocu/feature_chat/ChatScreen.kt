package com.example.appmuabandocu.feature_chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Block
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.ChatMessage
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.ui.theme.Blue_text_Dark
import com.example.appmuabandocu.viewmodel.ChatViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    receiverId: String,
    receiverName: String,
    productId: String = "",
    chatViewModel: ChatViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = androidx.compose.ui.platform.LocalContext.current
    val decodedName = remember(receiverName) {
        try {
            java.net.URLDecoder.decode(receiverName, "UTF-8")
        } catch (_: Exception) {
            receiverName
        }
    }

    val product = remember(productId, productViewModel.productList) {
        if (productId.isNotEmpty()) productViewModel.productList.find { it.id == productId } else null
    }

    var inputText by remember { mutableStateOf("") }
    val messageList = chatViewModel.messageList
    val isLoading by chatViewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(receiverId) {
        if (receiverId.isNotEmpty()) {
            chatViewModel.listenToMessages(receiverId)
        }
    }

    // Auto scroll to bottom when messages update
    LaunchedEffect(messageList.size) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.size - 1)
        }
    }

    var isUserBanned by remember { mutableStateOf(false) }
    var banReason by remember { mutableStateOf("") }
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(currentUserId).get().await()
                if (doc.getBoolean("isBanned") == true) {
                    isUserBanned = true
                    banReason = doc.getString("banReason") ?: "Vi phạm chính sách"
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = decodedName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Đang hoạt động",
                                fontSize = 11.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue_text
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
            // Product Banner if launched from a product
            product?.let { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable {
                            navController.navigate(Screen.ProductDetail.createRoute(p.id))
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val imgUrl = p.imageUrl.ifEmpty { p.imageMota.firstOrNull() ?: "" }
                        if (imgUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imgUrl.replace("http://", "https://"),
                                contentDescription = p.productName,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEEEEEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.productName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val priceStr = try {
                                val num = p.price.toLongOrNull() ?: 0L
                                NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(num) + " đ"
                            } catch (_: Exception) { "${p.price} đ" }
                            Text(
                                text = priceStr,
                                color = Color(0xFFFF5722),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        TextButton(onClick = {
                            // Quick question about product
                            chatViewModel.sendMessage(
                                otherUserId = receiverId,
                                otherUserName = decodedName,
                                otherUserAvatar = "",
                                text = "Chào bạn, sản phẩm \"${p.productName}\" này còn không ạ?",
                                product = p
                            )
                        }) {
                            Text("Hỏi còn hàng?", fontSize = 12.sp, color = Blue_text, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading && messageList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue_text)
                    }
                } else if (messageList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bắt đầu cuộc trò chuyện với $decodedName",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Trao đổi tình trạng đồ cũ, giá cả và địa điểm nhận hàng",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messageList, key = { it.id.ifEmpty { it.hashCode().toString() } }) { msg ->
                            val isMe = msg.senderId == currentUserId
                            ChatBubble(message = msg, isMe = isMe)
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                if (isUserBanned) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tài khoản bị khóa ($banReason). Không thể gửi tin nhắn.",
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Nhập tin nhắn...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue_text,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedContainerColor = Color.White
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val check = com.example.appmuabandocu.utils.ProfanityFilter.checkContent("", inputText)
                                if (!check.isValid) {
                                    android.widget.Toast.makeText(context, check.errorMessage, android.widget.Toast.LENGTH_SHORT).show()
                                    return@IconButton
                                }
                                chatViewModel.sendMessage(
                                    otherUserId = receiverId,
                                    otherUserName = decodedName,
                                    otherUserAvatar = "",
                                    text = inputText,
                                    product = product
                                )
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (inputText.isNotBlank()) Blue_text else Color(0xFFE0E0E0),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    val timeStr = remember(message.timestamp) {
        if (message.timestamp > 0) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        } else ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            color = if (isMe) Blue_text else Color.White,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                // Attached Product in message
                if (message.productId.isNotEmpty() && message.productName.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .background(
                                if (isMe) Color.White.copy(alpha = 0.2f) else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.productImage.isNotEmpty()) {
                            AsyncImage(
                                model = message.productImage.replace("http://", "https://"),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Column {
                            Text(
                                text = message.productName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (message.productPrice.isNotEmpty()) {
                                Text(
                                    text = "${message.productPrice} đ",
                                    fontSize = 11.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.9f) else Color(0xFFFF5722),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (message.message.isNotEmpty()) {
                    Text(
                        text = message.message,
                        color = if (isMe) Color.White else Color.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
