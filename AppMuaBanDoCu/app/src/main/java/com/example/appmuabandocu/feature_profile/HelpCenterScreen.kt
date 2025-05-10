package com.example.appmuabandocu.feature_profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text

data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(navController: NavController) {
    val safetyTips = listOf(
        "Giao dịch trực tiếp tại nơi công cộng đông người (quán cafe, trung tâm thương mại, cổng trường học).",
        "Luôn kiểm tra kỹ tình trạng đồ cũ (máy móc, phụ kiện, vết nứt, hóa đơn cũ) trước khi trả tiền.",
        "Không chuyển tiền đặt cọc trước cho người lạ nếu chưa xác minh được danh tính người bán.",
        "Sử dụng tính năng Nhắn tin và Gọi điện thoại trực tiếp trong ứng dụng để lưu lại bằng chứng trao đổi.",
        "Báo cáo ngay cho Admin nếu phát hiện dấu hiệu lừa đảo hoặc rao bán hàng cấm."
    )

    val faqs = listOf(
        FaqItem(
            "Làm thế nào để đăng bán một món đồ cũ?",
            "Tại màn hình chính hoặc thanh điều hướng dưới cùng, nhấn vào biểu tượng dấu cộng (+) Đăng bán. Sau đó chụp ảnh sản phẩm, điền tên, giá bán, mô tả, danh mục và địa chỉ của bạn, rồi nhấn Đăng bán."
        ),
        FaqItem(
            "Đăng tin bán hàng trên app có mất phí không?",
            "Hoàn toàn miễn phí! Ứng dụng Chợ Đồ Cũ hỗ trợ người dùng đăng tin thanh lý đồ đạc cá nhân không giới hạn số lượng bài đăng."
        ),
        FaqItem(
            "Làm sao để liên hệ người bán đồ?",
            "Trong trang chi tiết sản phẩm, bạn có thể nhấn nút 'Nhắn tin với người bán' để chat trực tiếp trong app, hoặc nhấn 'Gọi điện' để gọi số điện thoại người bán."
        ),
        FaqItem(
            "Khi đã bán được hàng, tôi cần làm gì?",
            "Bạn hãy vào mục 'Tài khoản' -> 'Quản lý bài viết' -> chọn món đồ đã bán và chuyển trạng thái thành 'Đã bán' hoặc 'Ẩn tin' để người khác không liên hệ làm phiền nữa."
        ),
        FaqItem(
            "Tôi phát hiện bài viết lừa đảo thì xử lý thế nào?",
            "Trong trang chi tiết sản phẩm, hãy nhấn nút 'Báo cáo vi phạm'. Đội ngũ Quản trị viên (Admin) sẽ kiểm tra và gỡ bỏ ngay lập tức."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trung Tâm Trợ Giúp",
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background_Light),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Safety Guidelines Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5 Quy Tắc Mua Bán Đồ Cũ An Toàn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        safetyTips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tip,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // FAQs Header
            item {
                Text(
                    text = "Câu Hỏi Thường Gặp (FAQ)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
            }

            items(faqs) { faq ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = faq.question,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = faq.answer,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
