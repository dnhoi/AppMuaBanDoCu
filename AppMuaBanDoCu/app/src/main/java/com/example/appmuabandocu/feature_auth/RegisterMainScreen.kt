package com.example.appmuabandocu.feature_auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appmuabandocu.R
import com.example.appmuabandocu.ui.theme.Blue_text
import android.widget.Toast
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.viewmodel.AuthViewModel
import com.example.appmuabandocu.viewmodel.AddressViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    addressViewModel: AddressViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
    ) {
    // Biến để lưu thông tin đăng ký
    val email by authViewModel.email.collectAsState()
    val fullName by authViewModel.fullName.collectAsState()
    val phoneNumber by authViewModel.phoneNumber.collectAsState()
    val password by authViewModel.password.collectAsState()

    // Thêm biến confirmPassword để xác nhận mật khẩu
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Biến để lưu danh sách tỉnh, quận, phường
    val provinces by addressViewModel.provinces.collectAsState()
    val districts by addressViewModel.districts.collectAsState()
    val wards by addressViewModel.wards.collectAsState()

    // Biến để lưu tỉnh, quận, phường đã chọn
    val selectedProvince by addressViewModel.selectedProvince.collectAsState()
    val selectedDistrict by addressViewModel.selectedDistrict.collectAsState()
    val selectedWard by addressViewModel.selectedWard.collectAsState()

    // Biến để quản lý trạng thái dropdown
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedWard by remember { mutableStateOf(false) }

    // Biến để theo dõi trạng thái đăng ký thành công
    val registerSuccess by authViewModel.registerSuccess.collectAsState()

    // Lấy context để hiển thị Toast
    val context = LocalContext.current

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            delay(1000)
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Register.route) {
                    inclusive = true
                }
            }
            authViewModel.resetRegisterSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo",
            )

            Text(
                text = "Đăng ký tài khoản",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Blue_text,
            )

            // Họ và tên
            OutlinedTextField(
                value = fullName,
                onValueChange = { authViewModel.setFullName(it) },
                label = { Text(text = "Họ và tên") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Số điện thoại
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { authViewModel.setPhoneNumber(it)},
                label = { Text(text = "Số điện thoại") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { authViewModel.setEmail(it) },
                label = { Text(text = "Email") },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Mật khẩu và xác nhận mật khẩu
            OutlinedTextField(
                value = password,
                onValueChange = { authViewModel.setPassword(it) },
                label = { Text(text = "Mật khẩu") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(text = "Xác nhận mật khẩu") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tỉnh/Thành phố Dropdown
            OutlinedTextField(
                value = selectedProvince?.name ?: "Chọn tỉnh",
                onValueChange = {},
                label = { Text("Tỉnh/Thành phố") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expandedProvince = !expandedProvince }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedIndicatorColor = Blue_text,
                    unfocusedIndicatorColor = Blue_text,
                    unfocusedContainerColor = Color.White,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )
            DropdownMenu(
                expanded = expandedProvince,
                onDismissRequest = { expandedProvince = false }
            ) {
                provinces.forEach { province ->
                    DropdownMenuItem(
                        onClick = {
                            addressViewModel.selectProvince(province)
                            expandedProvince = false
                        },
                        text = { Text(province.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quận/Huyện Dropdown
            if (selectedProvince != null) {
                OutlinedTextField(
                    value = selectedDistrict?.name ?: "Chọn quận/huyện",
                    onValueChange = {},
                    label = { Text("Quận/Huyện") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedDistrict = !expandedDistrict }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedIndicatorColor = Blue_text,
                        unfocusedIndicatorColor = Blue_text,
                        unfocusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                DropdownMenu(
                    expanded = expandedDistrict,
                    onDismissRequest = { expandedDistrict = false }
                ) {
                    districts.forEach { district ->
                        DropdownMenuItem(
                            onClick = {
                                addressViewModel.selectDistrict(district)
                                expandedDistrict = false
                            },
                            text = { Text(district.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Xã/Phường Dropdown
            if (selectedDistrict != null) {
                OutlinedTextField(
                    value = selectedWard?.name ?: "Chọn xã/phường",
                    onValueChange = {},
                    label = { Text("Xã/Phường") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedWard = !expandedWard }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedIndicatorColor = Blue_text,
                        unfocusedIndicatorColor = Blue_text,
                        unfocusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                DropdownMenu(
                    expanded = expandedWard,
                    onDismissRequest = { expandedWard = false }
                ) {
                    wards.forEach { ward ->
                        DropdownMenuItem(
                            onClick = {
                                addressViewModel.selectWard(ward)
                                expandedWard = false
                            },
                            text = { Text(ward.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thêm trường nhập địa chỉ đường/phố
                OutlinedTextField(
                    value = addressViewModel.streetAddress.collectAsState().value,
                    onValueChange = { addressViewModel.setStreetAddress(it) },
                    label = { Text("Số nhà, tên đường...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        focusedIndicatorColor = Blue_text,
                        unfocusedIndicatorColor = Blue_text,
                        unfocusedContainerColor = Color.White,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Kiểm tra thông tin địa chỉ
                    if (selectedProvince == null || selectedDistrict == null || selectedWard == null) {
                        Toast.makeText(context, "Vui lòng chọn đầy đủ địa chỉ!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (addressViewModel.streetAddress.value.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập địa chỉ cụ thể!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Lấy địa chỉ đầy đủ từ AddressViewModel
                    val fullAddress = buildString {
                        append(addressViewModel.streetAddress.value)
                        selectedWard?.let { append(", ${it.name}") }
                        selectedDistrict?.let { append(", ${it.name}") }
                        selectedProvince?.let { append(", ${it.name}") }
                    }

                    // Sử dụng ViewModel để đăng ký, truyền thêm địa chỉ
                    authViewModel.registerWithEmailPassword(
                        context,
                        fullAddress,
                        selectedProvince?.name ?: "",
                        selectedDistrict?.name ?: "",
                        selectedWard?.name ?: ""
                    )
                },
                modifier = Modifier.width(250.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue_text, contentColor = Color.White)
            ) {
                Text(text = "Đăng ký")
            }

            TextButton(onClick = { navController.navigate(Screen.Login.route){
                popUpTo(Screen.Register.route) {
                    inclusive = true
                }
            } }) {
                Text(text = "Đã có tài khoản? Đăng nhập ngay")
            }
        }
    }
}