package com.example.appmuabandocu.feature_add_product.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appmuabandocu.R
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.model.CategoryItem
import com.example.appmuabandocu.ui.theme.Blue_text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun CategoryScreen(auth: FirebaseAuth, navController: NavController) {
    val context = LocalContext.current
    val user = auth.currentUser

    val defaultCategories = remember {
        listOf("Thiết bị điện tử", "Xe cộ", "Quần áo", "Đồ gia dụng", "Khác")
    }
    var dynamicCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        val catRef = FirebaseDatabase.getInstance().getReference("categories")
        catRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                snapshot.children.forEach { child ->
                    val cat = child.getValue(CategoryItem::class.java)
                    cat?.name?.let { if (it.isNotBlank()) list.add(it) }
                }
                dynamicCategories = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val displayCategories = remember(dynamicCategories) {
        val merged = (defaultCategories + dynamicCategories).distinct()
        merged.filter { it != "Khác" } + (if (merged.contains("Khác")) listOf("Khác") else emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Chọn loại mặt hàng",
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = Bold,
            fontSize = 26.sp,
            color = Blue_text
        )
        HorizontalDivider(
            color = Blue_text.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (user == null) {
            navController.navigate(Screen.Login.route)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayCategories) { category ->
                    CategoryOption(title = category) {
                        navController.navigate("add_product_screen/$category")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryOption(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Icon(painterResource(id = R.drawable.ic_next), contentDescription = "Next", tint = Blue_text)
        }
    }
}
