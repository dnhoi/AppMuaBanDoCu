package com.example.appmuabandocu.feature_auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appmuabandocu.R
import com.example.appmuabandocu.ui.theme.Blue_text
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    navController: NavController,
    ) {
    val context = LocalContext.current

    val user = FirebaseAuth.getInstance().currentUser

    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val googleSignOptions = authViewModel.getGoogleSignInOptions(context)
    val googleSignInClient = GoogleSignIn.getClient(context, googleSignOptions)


    val banDialogMessage by authViewModel.banDialogMessage.collectAsState()

    if (banDialogMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { authViewModel.clearBanDialogMessage() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Thông Báo Khóa Tài Khoản",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(text = banDialogMessage ?: "")
            },
            confirmButton = {
                Button(
                    onClick = { authViewModel.clearBanDialogMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_text)
                ) {
                    Text("Đã hiểu", color = Color.White)
                }
            }
        )
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Sign in with Google
                authViewModel.signInWithGoogle(account, context)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoggedIn == false) {
        // Nền tối giản với màu nhẹ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FCFF))  // Màu nền rất nhẹ
        ) {
            // Thêm một số hình trang trí tối giản
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Blue_text.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Content với card nổi bật
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )

                // Card chứa form đăng nhập
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = "Đăng nhập",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blue_text,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { authViewModel.setEmail(it) },
                            label = { Text(text = "Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Blue_text
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                focusedIndicatorColor = Blue_text,
                                unfocusedIndicatorColor = Blue_text,
                                focusedContainerColor = Color(0xFFF8FCFF),
                                unfocusedContainerColor = Color(0xFFF8FCFF),
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = Blue_text,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { authViewModel.setPassword(it) },
                            label = { Text(text = "Mật khẩu") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password Icon",
                                    tint = Blue_text
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Filled.VisibilityOff
                                        else
                                            Icons.Filled.Visibility,
                                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                        tint = Blue_text
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                focusedIndicatorColor = Blue_text,
                                unfocusedIndicatorColor = Blue_text,
                                focusedContainerColor = Color(0xFFF8FCFF),
                                unfocusedContainerColor = Color(0xFFF8FCFF),
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = Blue_text,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )

                        // Forgot password
                        TextButton(
                            onClick = { /* Xử lý quên mật khẩu */ },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(bottom = 24.dp)
                        ) {
                            Text(
                                text = "Quên mật khẩu?",
                                fontSize = 14.sp,
                                color = Blue_text,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Login button
                        Button(
                            onClick = {
                                authViewModel.signInWithEmailPassword(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue_text,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Text(
                                text = "Đăng nhập",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Hoặc",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.Gray.copy(alpha = 0.5f)
                    )
                }

                // Google sign in button
                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Icon"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Đăng nhập với Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Register link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chưa có tài khoản?",
                        color = Color.Gray
                    )
                    TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                        Text(
                            text = "Đăng ký ngay",
                            color = Blue_text,
                            fontWeight = Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
