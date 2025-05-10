package com.example.appmuabandocu.feature_profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appmuabandocu.R
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.viewmodel.ProfileViewModel
import com.example.appmuabandocu.ui.theme.Blue_text
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    navController: NavController,
    auth: FirebaseAuth,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user = auth.currentUser

    val isLoading by profileViewModel.isLoading.collectAsState()
    val avatarUrl by profileViewModel.avatarUrl.collectAsState()
    val message by profileViewModel.message.collectAsState()
    val userData by profileViewModel.userData.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    var fullName by remember(userData) { mutableStateOf(userData?.fullName ?: user?.displayName ?: "") }
    var phoneNumber by remember(userData) { mutableStateOf(userData?.phoneNumber ?: "") }
    var address by remember(userData) { mutableStateOf(userData?.address ?: "") }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearMessage()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileViewModel.uploadProfileImage(context, uri)
        }
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
                        .statusBarsPadding()
                        .shadow(4.dp),
                    color = Color(0xFFFFB74D) // Cam đất nhạt cho TopBar
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Thông tin cá nhân",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.back_left),
                                    contentDescription = "Quay lại",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            containerColor = Background_Light
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Avatar và nút chỉnh sửa
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            if (avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = avatarUrl.replace("http://", "https://"),
                                    contentDescription = "Ảnh đại diện",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE0E0E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        // Nút chỉnh sửa ảnh
                        FloatingActionButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp),
                            containerColor = Blue_text,
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa ảnh đại diện",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hiển thị loading
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Blue_text
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Đang xử lý...",
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // Form thông tin người dùng
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Thông tin cơ bản",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Blue_text
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Tên người dùng
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = fullName,
                                    onValueChange = { if (isEditing) fullName = it },
                                    label = { Text("Tên người dùng") },
                                    readOnly = !isEditing,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isEditing) Blue_text else Color.Gray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text,
                                        unfocusedLabelColor = Color.Gray,
                                        cursorColor = if (isEditing) Blue_text else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Email
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = userData?.email ?: user?.email ?: "",
                                    onValueChange = { },
                                    label = { Text("Email") },
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text,
                                        unfocusedLabelColor = Color.Gray
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Thông tin liên hệ
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Thông tin liên hệ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Blue_text
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Số điện thoại
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = phoneNumber,
                                    onValueChange = { if (isEditing) phoneNumber = it },
                                    label = { Text("Số điện thoại") },
                                    readOnly = !isEditing,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = if (isEditing) Blue_text else Color.Gray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text,
                                        unfocusedLabelColor = Color.Gray,
                                        cursorColor = if (isEditing) Blue_text else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Địa chỉ
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = address,
                                    onValueChange = { if (isEditing) address = it },
                                    label = { Text("Địa chỉ") },
                                    readOnly = !isEditing,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (isEditing) Blue_text else Color.Gray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text,
                                        unfocusedLabelColor = Color.Gray,
                                        cursorColor = if (isEditing) Blue_text else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nút chỉnh sửa/lưu
                        Button(
                            onClick = {
                                if (isEditing) {
                                    val updatedUser = userData?.copy(
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        address = address
                                    ) ?: User(
                                        email = user?.email ?: "",
                                        fullName = fullName,
                                        phoneNumber = phoneNumber,
                                        address = address
                                    )
                                    profileViewModel.updateUserProfile(updatedUser)
                                }
                                isEditing = !isEditing
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue_text
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isEditing) "Lưu thông tin" else "Chỉnh sửa thông tin",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Hiển thị loading overlay khi đang xử lý
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blue_text)
                    }
                }
            }
        }
    }
}