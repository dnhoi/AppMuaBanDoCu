package com.example.appmuabandocu.feature_add_product.ui

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.tasks.await
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Desk
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.data.uploadImageToCloudinary
import com.example.appmuabandocu.feature_add_product.CurrencyInputTransformation
import com.example.appmuabandocu.ui.theme.Background_Light
import com.example.appmuabandocu.ui.theme.Blue_text
import com.example.appmuabandocu.viewmodel.AddressViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Throws(IOException::class)
fun createImageFile(context: Context): File {
    // Tạo tên file ảnh
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    // Tạo file ảnh tạm thời
    val image = File.createTempFile(
        "JPEG_${timeStamp}_",  /* Tiền tố của tên file */
        ".jpg",                /* Đuôi file */
        storageDir             /* Thư mục lưu file */
    )

    // Trả về đường dẫn file ảnh
    return image
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    modifier: Modifier = Modifier,
    category: String,
    addressViewModel: AddressViewModel = viewModel(),
    viewModel: ProductViewModel = viewModel(),
    navController: NavController
) {
    val Blue_text = com.example.appmuabandocu.ui.theme.Blue_text
    val orange = com.example.appmuabandocu.ui.theme.Coral_Orange
    val lightGray = Color(0xFFF5F5F5)
    val darkGray = Color(0xFF757575)

    // Quan sát trạng thái thông báo từ ViewModel
    val message = viewModel.message.collectAsState().value
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var numberUser by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var provinceFB by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var negotiable by remember { mutableStateOf(false) }
    var freeShip by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }


    // Biến để lưu danh sách tỉnh, quận, phường
    val provinces by addressViewModel.provinces.collectAsState()
    val districts by addressViewModel.districts.collectAsState()
    val wards by addressViewModel.wards.collectAsState()

    // Biến để lưu tỉnh, quận, phường đã chọn
    val selectedProvince by addressViewModel.selectedProvince.collectAsState()
    val selectedDistrict by addressViewModel.selectedDistrict.collectAsState()
    val selectedWard by addressViewModel.selectedWard.collectAsState()

    // Biến để lưu trạng thái lỗi số điện thoại
    var phoneError by remember { mutableStateOf(false) }

    // Thêm các biến để quản lý dropdown danh mục
    var selectedCategory by remember { mutableStateOf(category) }
    var expandedCategory by remember { mutableStateOf(false) }

    // Danh sách các danh mục
    val categories = listOf("Thiết bị điện tử", "Xe cộ", "Quần áo", "Đồ gia dụng", "Khác")

    var isPosting by remember { mutableStateOf(false) }
    val isFormValid = productName.isNotBlank() &&
            selectedCategory.isNotBlank() &&
            price.isNotBlank() &&
            imageUris.isNotEmpty() &&
            address.isNotBlank() &&
            numberUser.isNotBlank() &&
            details.isNotBlank()

    // Biến để quản lý trạng thái dropdown
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedWard by remember { mutableStateOf(false) }

    provinceFB = selectedProvince?.name ?: ""

    address = buildString {
        if (selectedWard != null) append("${selectedWard!!.name}, ")
        if (selectedDistrict != null) append("${selectedDistrict!!.name}, ")
        if (selectedProvince != null) append(selectedProvince!!.name)
    }





    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> imageUris = imageUris + uris }


    // Lấy thông tin người dùng từ FirebaseAuth
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
    val userAvatar = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString() ?: ""

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    fun onImagePicked(uri: Uri) {
        // Thêm ảnh vào danh sách ảnh đã chọn
        imageUris = imageUris + uri
    }
    // Sử dụng rememberLauncherForActivityResult để mở camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let { uri ->
                // Xử lý ảnh sau khi chụp
                onImagePicked(uri) // Giả sử bạn có hàm xử lý ảnh đã chọn
            }
        }
    }

//
//    // SnackbarHostState để hiển thị Snackbar
//    val snackbarHostState = remember { SnackbarHostState() }
//
//
//    // Khi có message mới, hiển thị Snackbar
//    LaunchedEffect(message) {
//        if (message.isNotEmpty()) {
//            snackbarHostState.showSnackbar(message)
//        }
//    }


    // Reset UI khi nhận thông báo thành công
    LaunchedEffect(message) {
        if (message == "Sản phẩm đã được đăng thành công!") {
            productName = ""
            price = ""
            address = ""
            provinceFB = ""
            numberUser = ""
            details = ""
            negotiable = false
            freeShip = false
            imageUris = emptyList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Đăng bán sản phẩm",
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
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(lightGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Blue_text
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hình ảnh sản phẩm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = " (${imageUris.size}/10)",
                                color = darkGray,
                                fontSize = 14.sp
                            )
                        }
                        if (imageUris.isEmpty()) {
                            // Hiển thị nút thêm ảnh nếu chưa có ảnh nào
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(lightGray, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Thêm ảnh",
                                        modifier = Modifier.size(48.dp),
                                        tint = darkGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Thêm ảnh sản phẩm",
                                        color = darkGray
                                    )
                                }
                            }
                        } else {
                            // Hiển thị danh sách ảnh đã chọn
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(imageUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "Selected Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(
                                                    1.dp,
                                                    Color.LightGray,
                                                    RoundedCornerShape(8.dp)
                                                )
                                        )

                                        // Nút xóa ảnh
                                        IconButton(
                                            onClick = {
                                                imageUris = imageUris.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Xóa ảnh",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                // Nút thêm ảnh nếu chưa đủ 10 ảnh
                                if (imageUris.size < 10) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .background(lightGray, RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    Color.LightGray,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { imagePicker.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Thêm ảnh",
                                                tint = darkGray,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Nút chọn ảnh từ thư viện hoặc chụp ảnh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Nút chọn ảnh từ thư viện
                            OutlinedButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Blue_text),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Blue_text
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Thư viện",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thư viện")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Nút chụp ảnh
                            OutlinedButton(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        createImageFile(context)
                                    )
                                    cameraImageUri.value = uri
                                    cameraLauncher.launch(uri)
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Blue_text),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Blue_text
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }
                        }
                    }
                }
                // Phần thông tin sản phẩm
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Blue_text
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thông tin sản phẩm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        // Trường nhập tên sản phẩm
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Tên sản phẩm") },
                            placeholder = { Text("Nhập tên sản phẩm") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue_text,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Blue_text
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = darkGray
                                )
                            }
                        )
                        // Danh mục sản phẩm
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory,
                            onExpandedChange = { expandedCategory = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Danh mục sản phẩm") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = darkGray
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Blue_text,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Blue_text
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedCategory = category
                                            expandedCategory = false
                                        },
                                        trailingIcon = {
                                            if (selectedCategory == category) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Blue_text
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Trường nhập giá bán sản phẩm
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } || input.isEmpty()) {
                                        price = input
                                    }
                                },
                                label = { Text("Giá bán") },
                                placeholder = { Text("Nhập giá bán") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Blue_text,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Blue_text
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = darkGray
                                    )
                                },
                                trailingIcon = {
                                    Text(
                                        text = "VNĐ",
                                        color = darkGray,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            )
                        }

                        // Trường giá có thể thương lượng
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { negotiable = !negotiable },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = negotiable,
                                onCheckedChange = { negotiable = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Blue_text,
                                    uncheckedColor = darkGray
                                )
                            )
                            Text(
                                text = "Giá có thể thương lượng",
                                fontSize = 14.sp
                            )
                        }

                        // Trường phí ship miễn phí
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { freeShip = !freeShip },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = freeShip,
                                onCheckedChange = { freeShip = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Blue_text,
                                    uncheckedColor = darkGray
                                )
                            )
                            Text(
                                text = "Miễn phí vận chuyển",
                                fontSize = 14.sp
                            )
                        }

                        // Trường thông tin chi tiết sản phẩm
                        OutlinedTextField(
                            value = details,
                            onValueChange = { details = it },
                            label = { Text("Thông tin chi tiết") },
                            placeholder = { Text("Mô tả chi tiết sản phẩm của bạn ... ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue_text,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Blue_text
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = darkGray,
                                )
                            }
                        )
                    }
                }
                // Thông tin liên hệ
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Blue_text
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thông tin liên hệ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        // Trường nhập số điện thoại
                        OutlinedTextField(
                            value = numberUser,
                            onValueChange = {
                                if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                                    numberUser = it
                                    phoneError = it.length < 10
                                }
                            },
                            isError = phoneError,
                            label = { Text("Số điện thoại") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue_text,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Blue_text,
                                errorBorderColor = Color(0xFFFFA726),
                                errorLabelColor = Color(0xFFFFA726)
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (phoneError) Color(0xFFFFA726) else darkGray
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            supportingText = {
                                AnimatedVisibility(visible = phoneError) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = Color(0xFFFFA726),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Số điện thoại không hợp lệ",
                                            color = Color(0xFFFFA726),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        )
                        // Tỉnh/Thành phố Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedProvince,
                            onExpandedChange = { expandedProvince = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedProvince?.name ?: "Chọn Tỉnh/Thành phố‘",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tỉnh/Thành phố") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvince)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationCity,
                                        contentDescription = null,
                                        tint = darkGray
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Blue_text,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Blue_text
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedProvince,
                                onDismissRequest = { expandedProvince = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .heightIn(max = 300.dp)
                            ) {
                                provinces.forEach { province ->
                                    DropdownMenuItem(
                                        text = { Text(province.name) },
                                        onClick = {
                                            addressViewModel.selectProvince(province)
                                            provinceFB = province.name
                                            expandedProvince = false
                                        },
                                        trailingIcon = {
                                            if (selectedProvince == province) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Blue_text
                                                )
                                            }
                                            IconButton(onClick = {
                                                expandedProvince = !expandedProvince
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Quận/Huyện Dropdown
                        if (selectedProvince != null) {
                            ExposedDropdownMenuBox(
                                expanded = expandedDistrict,
                                onExpandedChange = { expandedDistrict = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedDistrict?.name ?: "Quận/Huyện",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Quận/Huyện") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDistrict)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationCity,
                                            contentDescription = null,
                                            tint = darkGray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text
                                    ),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedDistrict,
                                    onDismissRequest = { expandedDistrict = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .heightIn(max = 300.dp)
                                ) {
                                    districts.forEach { district ->
                                        DropdownMenuItem(
                                            text = { Text(district.name) },
                                            onClick = {
                                                addressViewModel.selectDistrict(district)
                                                expandedDistrict = false
                                            },
                                            trailingIcon = {
                                                if (selectedDistrict == district) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Blue_text
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        // Xã/Phường Dropdown
                        if (selectedDistrict != null) {
                            ExposedDropdownMenuBox(
                                expanded = expandedWard,
                                onExpandedChange = { expandedWard = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedWard?.name ?: "Chọn Xã/Phường",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Xã/Phường") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWard)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = darkGray
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue_text,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Blue_text
                                    ),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedWard,
                                    onDismissRequest = { expandedWard = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .heightIn(max = 300.dp)
                                ) {
                                    wards.forEach { ward ->
                                        DropdownMenuItem(
                                            text = { Text(ward.name) },
                                            onClick = {
                                                addressViewModel.selectWard(ward)
                                                expandedWard = false
                                            },
                                            trailingIcon = {
                                                if (selectedWard == ward) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Blue_text
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                        }

                        // Trường nhập địa chỉ chi tiết
                        OutlinedTextField(
                            value = addressViewModel.streetAddress.collectAsState().value,
                            onValueChange = { addressViewModel.setStreetAddress(it) },
                            label = { Text("Số nhà, Tên đường, ... ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Blue_text,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Blue_text
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = darkGray
                                )
                            }
                        )
                    }
                }
            }
            val coroutineScope = rememberCoroutineScope()
            // Nút đăng sản phẩm
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .shadow(4.dp)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isPosting) return@Button
                        if (productName.isEmpty() || price.isEmpty() || address.isEmpty() || numberUser.isEmpty() || details.isEmpty()) {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Bộ lọc tự động từ khóa cấm, mặt hàng cấm & danh sách đen
                        val checkResult = com.example.appmuabandocu.utils.ProfanityFilter.checkContent(productName, details, price)
                        if (!checkResult.isValid) {
                            Toast.makeText(context, checkResult.errorMessage, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        if (imageUris.isEmpty()) {
                            Toast.makeText(context, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isPosting = true
                        coroutineScope.launch {
                            // Kiểm tra xem tài khoản có bị Ban Quản Trị khóa hay không
                            try {
                                val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("users").document(userId).get().await()
                                if (userDoc.getBoolean("isBanned") == true) {
                                    val banReason = userDoc.getString("banReason") ?: "Vi phạm tiêu chuẩn cộng đồng"
                                    Toast.makeText(context, "Tài khoản của bạn đã bị khóa ($banReason). Không thể đăng bài!", Toast.LENGTH_LONG).show()
                                    isPosting = false
                                    return@launch
                                }
                            } catch (_: Exception) {}

                            val urls = imageUris.mapNotNull { uri ->
                                uploadImageToCloudinary(context, uri)
                            }
                            if (urls.isNotEmpty()) {
                                val product = Product(
                                    productName = productName,
                                    price = price,
                                    address = address,
                                    provinceFB = provinceFB,
                                    numberUser = numberUser,
                                    category = selectedCategory,
                                    details = details,
                                    negotiable = negotiable,
                                    freeShip = freeShip,
                                    imageUrl = urls[0],
                                    imageMota = urls.drop(1),
                                    userId = userId,
                                    userName = userName,
                                    userAvatar = userAvatar
                                )
                                viewModel.postProduct(product)
                                Toast.makeText(context, "Đăng bài viết thành công", Toast.LENGTH_SHORT).show()
                                delay(2000)
                            } else {
                                Toast.makeText(context, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            isPosting = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = isFormValid && !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Đăng bán",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
