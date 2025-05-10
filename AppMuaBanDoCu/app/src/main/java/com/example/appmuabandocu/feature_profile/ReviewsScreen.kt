package com.example.appmuabandocu.feature_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.model.Review
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.viewmodel.ReviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    navController: NavController,
    userId: String,
    userName: String,
    viewModel: ReviewViewModel = viewModel()
) {
    val reviewList = viewModel.reviewList
    val averageRating by viewModel.averageRating
    val totalReviews by viewModel.totalReviews
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showAddReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    val decodedUserName = remember(userName) {
        try {
            java.net.URLDecoder.decode(userName, "UTF-8")
        } catch (_: Exception) {
            userName
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadReviewsForSeller(userId)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Add Review Dialog
    if (showAddReviewDialog) {
        AlertDialog(
            onDismissRequest = { showAddReviewDialog = false },
            title = {
                Text(text = "Đánh giá $decodedUserName", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    Text(text = "Chọn số sao đánh giá độ uy tín:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { reviewRating = i }) {
                                Icon(
                                    imageVector = if (i <= reviewRating) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = "$i sao",
                                    tint = if (i <= reviewRating) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Nhận xét (ví dụ: Giao đồ đúng hẹn, hàng như mô tả)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewComment.isNotBlank()) {
                            viewModel.submitReview(
                                sellerId = userId,
                                rating = reviewRating,
                                comment = reviewComment
                            )
                            showAddReviewDialog = false
                            reviewComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text)
                ) {
                    Text("Gửi đánh giá", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddReviewDialog = false }) {
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
                    Text(
                        text = "Đánh Giá & Uy Tín",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue_text)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddReviewDialog = true },
                containerColor = Blue_text,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Default.RateReview, contentDescription = null) },
                text = { Text("Viết đánh giá", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background_Light)
        ) {
            // Rating Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", averageRating),
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = Color(0xFFFF9800)
                        )
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= averageRating.toInt()) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "$totalReviews lượt nhận xét",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "Độ tin cậy người bán",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dựa trên phản hồi từ những người mua đồ cũ thực tế",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Reviews List
            if (reviewList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có lượt đánh giá nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hãy là người đầu tiên đánh giá trải nghiệm giao dịch với $decodedUserName!",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reviewList, key = { it.id.ifEmpty { it.hashCode().toString() } }) { review ->
                        ReviewItemCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(review: Review) {
    val dateStr = remember(review.timestamp) {
        if (review.timestamp > 0) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(review.timestamp))
        } else ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.reviewerAvatar.isNotEmpty()) {
                        AsyncImage(
                            model = review.reviewerAvatar.replace("http://", "https://"),
                            contentDescription = review.reviewerName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.reviewerName.ifEmpty { "Người mua ẩn danh" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
