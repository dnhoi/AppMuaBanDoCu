package com.example.appmuabandocu.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.text.category
import kotlin.text.lowercase


import com.example.appmuabandocu.model.BannerItem
import com.example.appmuabandocu.model.CategoryItem

class ProductViewModel : ViewModel() {

    private val _productList = mutableStateListOf<Product>()
    val productList: List<Product> get() = _productList

    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")
    private val bannersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("banners")
    private val categoriesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("categories")

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _bannerList = MutableStateFlow<List<BannerItem>>(emptyList())
    val bannerList: StateFlow<List<BannerItem>> = _bannerList.asStateFlow()

    private val _categoryList = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoryList: StateFlow<List<CategoryItem>> = _categoryList.asStateFlow()

    init {
        loadProductsRealtime()
        loadBanners()
        loadCategories()
    }

    private val _productsByCategory = MutableStateFlow<List<Product>>(emptyList())
    val productsByCategory: StateFlow<List<Product>> = _productsByCategory

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> get() = _searchResults

    fun loadBanners() {
        bannersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerItem>()
                snapshot.children.forEach { child ->
                    child.getValue(BannerItem::class.java)?.let {
                        if (it.isActive) list.add(it)
                    }
                }
                _bannerList.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error loading banners: ${error.message}")
            }
        })
    }

    fun loadCategories() {
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryItem>()
                snapshot.children.forEach { child ->
                    child.getValue(CategoryItem::class.java)?.let { list.add(it) }
                }
                _categoryList.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductViewModel", "Error loading categories: ${error.message}")
            }
        })
    }


    // Tải dữ liệu sản phẩm từ Firebase Realtime Database
    fun loadProductsRealtime() {
        isLoading.value = true

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newProducts = mutableListOf<Product>()
                snapshot.children.forEach { childSnapshot ->
                    val product = childSnapshot.getValue(Product::class.java)
                    product?.let { newProducts.add(it) }
                }
                _productList.clear()
                _productList.addAll(newProducts)
                isLoading.value = false
                Log.d("RealtimeDatabase", "Dữ liệu đã cập nhật theo thời gian thực!")
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                errorMessage.value = "Lỗi khi tải dữ liệu realtime: ${error.message}"
                Log.e("RealtimeDatabase", "Lỗi realtime: ", error.toException())
            }
        })
    }


    // Đăng sản phẩm mới lên Firebase
    fun postProduct(product: Product) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val productWithTimeAndStock = product.copy(
            timestamp = Date().time,
            inStock = true
        )

        if (userId != null) {
            val productRef = db.push() // tạo node mới, tự sinh id
            val productId = productRef.key ?: return

            // Gán userId và id vào product
            val productData = product.copy(
                id = productId,
                userId = userId,
                timestamp = Date().time,
                inStock = true,
                status = "available" // Mặc định trạng thái là "available"
            )

            productRef.setValue(productData)
                .addOnSuccessListener {
                    Log.d("RealtimeDatabase", "Sản phẩm đã được đăng thành công!")
                    _message.value = "Sản phẩm đã được đăng thành công!"
                    loadProductsRealtime()
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Lỗi đăng sản phẩm: ${e.message}"
                    Log.e("RealtimeDatabase", "Lỗi đăng sản phẩm: ", e)
                    _message.value = "Lỗi đăng sản phẩm: ${e.message}"
                }
        } else {
            errorMessage.value = "Người dùng chưa đăng nhập!"
            Log.e("RealtimeDatabase", "Người dùng chưa đăng nhập!")
            _message.value = "Người dùng chưa đăng nhập!"
        }
    }


    // Hàm để thay đổi trạng thái ẩn/hiện của một sản phẩm
    fun toggleProductVisibility(productId: String) {
        _productList.find { it.id == productId }?.let {
            val updatedProduct = it.copy(displayed = it.displayed?.not()) // Đảo trạng thái hiển thị
            _productList[_productList.indexOf(it)] = updatedProduct
        }
    }

    // Hàm để lấy danh sách sản phẩm (chỉ trả về sản phẩm không bị ẩn)
    fun getVisibleProducts(): List<Product> {
        return _productList
    }

    // Hàm để lấy danh sách sản phẩm theo trạng thái hiển thị
    fun loadVisibleProducts(): List<Product> {
        return _productList.filter {
            it.status == "available" || it.status == "sold"
        }
    }

    // Cập nhật danh sách sản phẩm (ví dụ: gọi từ Firestore)
    fun setProductList(products: List<Product>) {
        _productList.clear()
        _productList.addAll(products)
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Giả sử gọi lại API hoặc Firebase
            loadProductsRealtime() // hoặc loadVisibleProducts()
            delay(1000) // delay 1s để hiển thị hiệu ứng rõ ràng (tùy)
            _isRefreshing.value = false
        }
    }

    fun searchProducts(query: String) {
        val keyword = query.trim().lowercase()
        val filtered = if (keyword.isBlank()) {
            _productList.filter { product ->
                product.status == "available" || product.status == "sold"
            }
        } else {
            _productList.filter { product ->
                // Lọc theo từ khóa và trạng thái
                (product.status == "available" || product.status == "sold") && (
                        product.productName?.lowercase()?.contains(keyword) == true ||
                                product.address?.lowercase()?.contains(keyword) == true ||
                                product.userName?.lowercase()?.contains(keyword) == true ||
                                product.category?.lowercase()?.contains(keyword) == true
                        )
            }
        }
        _searchResults.value = filtered
    }

    fun getProductsByCategory(category: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("products")

        dbRef.orderByChild("category").equalTo(category)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = mutableListOf<Product>()
                    for (child in snapshot.children) {
                        val product = child.getValue(Product::class.java)
                        product?.let { productList.add(it) }
                    }
                    _productsByCategory.value = productList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }


}

