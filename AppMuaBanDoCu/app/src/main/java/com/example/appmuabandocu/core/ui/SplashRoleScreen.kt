package com.example.appmuabandocu.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appmuabandocu.R
import com.example.appmuabandocu.ui.theme.Blue_text
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.appmuabandocu.core.navigation.model.Screen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import com.example.appmuabandocu.ui.theme.Blue_text_Dark

@Composable
fun SplashRoleScreen(
    navController: NavController
) {
    var loginButtonElevation by remember { mutableStateOf(8.dp) }
    var exploreButtonElevation by remember { mutableStateOf(4.dp) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F9FF),
                            Color(0xFFE6F7FF)
                        )
                    )
                )
        )

        // Pattern overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (x in 0..size.width.toInt() step 50) {
                for (y in 0..size.height.toInt() step 50) {
                    drawCircle(
                        color = Color(0xFF1E40AF).copy(alpha = 0.05f),
                        radius = 3f,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }

        // Hình trang trí 1
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
        )

        // Hình trang trí 2
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF1E40AF).copy(alpha = 0.1f), CircleShape)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 50.dp)
        )

        // Nội dung chính
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(CircleShape)  // Hoặc sử dụng hình dạng phù hợp với logo
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_app),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // Text "Chào mừng bạn đến với" - đơn giản và rõ ràng
                Text(
                    text = "Chào mừng bạn đến với",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )

                // Text "Thanh Lý Nhanh" - hiện đại và nghiêm túc, không cần Box hay hiệu ứng phức tạp
                Text(
                    text = "Thanh Lý Nhanh",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Blue_text,
                                Blue_text_Dark
                            )
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Text "Nơi mua bán..." - rõ ràng và nghiêm túc
                Text(
                    text = "Nơi mua bán, trao đổi đồ cũ uy tín",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Nút đăng nhập với gradient và hiệu ứng nhấn
            // Nút đăng nhập với màu chủ đạo Blue_text
            Button(
                onClick = {
                    navController.navigate(Screen.Login.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = loginButtonElevation,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Blue_text.copy(alpha = 0.5f)  // Sử dụng Blue_text thay vì 0xFF3B82F6
                    )
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        loginButtonElevation = 4.dp
                                    }

                                    PointerEventType.Release -> {
                                        loginButtonElevation = 8.dp
                                    }
                                }
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Blue_text,           // Sử dụng Blue_text
                                    Blue_text_Dark       // Sử dụng Blue_text_Dark
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đăng nhập",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

// Nút khám phá với hiệu ứng nhấn
            Button(
                onClick = {
                    navController.navigate(Screen.Home.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Blue_text_Dark  // Sử dụng Blue_text_Dark thay vì 0xFF1E40AF
                ),
                border = BorderStroke(1.dp, Blue_text),  // Sử dụng Blue_text thay vì 0xFF1E40AF
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = exploreButtonElevation,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when {
                                    event.type == PointerEventType.Press -> {
                                        exploreButtonElevation = 2.dp
                                    }

                                    event.type == PointerEventType.Release -> {
                                        exploreButtonElevation = 4.dp
                                    }
                                }
                            }
                        }
                    }
            ) {
                Text(
                    text = "Khám phá ngay",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}