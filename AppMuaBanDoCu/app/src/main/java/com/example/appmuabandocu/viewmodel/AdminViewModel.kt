package com.example.appmuabandocu.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.BannerItem
import com.example.appmuabandocu.model.CategoryItem
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {

    private val realtimeDb = FirebaseDatabase.getInstance().getReference("products")
    private val reportsRef = FirebaseDatabase.getInstance().getReference("reports")
    private val bannersRef = FirebaseDatabase.getInstance().getReference("banners")
    private val categoriesRef = FirebaseDatabase.getInstance().getReference("categories")
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val productsCollection = firestore.collection("products")

    private val _productList = mutableStateListOf<Product>()
    val productList: List<Product> get() = _productList

    private val _userList = mutableStateListOf<User>()
    val userList: List<User> get() = _userList

    private val _reportList = mutableStateListOf<com.example.appmuabandocu.model.Report>()
    val reportList: List<com.example.appmuabandocu.model.Report> get() = _reportList

    private val _bannerList = mutableStateListOf<BannerItem>()
    val bannerList: List<BannerItem> get() = _bannerList

    private val _categoryList = mutableStateListOf<CategoryItem>()
    val categoryList: List<CategoryItem> get() = _categoryList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val productToDelete = mutableStateOf<Product?>(null)
    val userToToggleRole = mutableStateOf<User?>(null)
    val reportToProcess = mutableStateOf<com.example.appmuabandocu.model.Report?>(null)

    private var usersListener: ListenerRegistration? = null
    private var productsListener: ValueEventListener? = null
    private var reportsListener: ValueEventListener? = null
    private var bannersListener: ValueEventListener? = null
    private var categoriesListener: ValueEventListener? = null

    init {
        loadAllProducts()
        loadAllUsers()
        loadAllReports()
        loadAllBanners()
        loadAllCategories()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun loadAllProducts() {
        _isLoading.value = true
        productsListener = realtimeDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { child ->
                    val product = child.getValue(Product::class.java)
                    product?.let { list.add(it) }
                }
                _productList.clear()
                _productList.addAll(list)
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _message.value = "Lỗi tải sản phẩm: ${error.message}"
                Log.e("AdminViewModel", "Error loading products: ${error.message}")
            }
        })
    }

    private fun loadAllUsers() {
        usersListener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("AdminViewModel", "Error loading users: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val emailStr = data["email"] as? String ?: ""
                    val isAdminVal = (data["isAdmin"] as? Boolean)
                        ?: (emailStr.startsWith("admin@") || emailStr == "admin@gmail.com")
                    val isBannedVal = (data["isBanned"] as? Boolean) ?: false
                    val banReasonVal = data["banReason"] as? String ?: ""
                    val bannedUntilVal = (data["bannedUntil"] as? Number)?.toLong() ?: 0L
                    User(
                        uid = data["id"] as? String ?: doc.id,
                        email = emailStr,
                        fullName = data["name"] as? String ?: "",
                        avatarUrl = data["avatarUrl"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String,
                        address = data["address"] as? String,
                        province = data["province"] as? String,
                        district = data["district"] as? String,
                        ward = data["ward"] as? String,
                        isAdmin = isAdminVal,
                        isBanned = isBannedVal,
                        banReason = banReasonVal,
                        bannedUntil = bannedUntilVal
                    )
                }
                _userList.clear()
                _userList.addAll(list)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (product.id.isNotEmpty()) {
                    // Xóa khỏi Realtime Database
                    realtimeDb.child(product.id).removeValue().await()
                    // Đồng bộ xóa Firestore nếu có
                    try {
                        productsCollection.document(product.id).delete().await()
                    } catch (_: Exception) {}
                    _message.value = "Đã xóa bài viết vi phạm: \"${product.productName ?: "Sản phẩm"}\""
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Lỗi xóa sản phẩm", e)
                _message.value = "Lỗi xóa bài viết: ${e.message}"
            } finally {
                _isLoading.value = false
                productToDelete.value = null
            }
        }
    }

    fun toggleProductVisibility(product: Product) {
        viewModelScope.launch {
            try {
                val newStatus = !(product.displayed ?: true)
                val updated = product.copy(displayed = newStatus)
                if (product.id.isNotEmpty()) {
                    realtimeDb.child(product.id).setValue(updated).await()
                    val msg = if (newStatus) "Đã hiện bài viết" else "Đã ẩn bài viết vi phạm"
                    _message.value = msg
                }
            } catch (e: Exception) {
                _message.value = "Lỗi cập nhật hiển thị: ${e.message}"
            }
        }
    }

    fun toggleAdminRole(user: User) {
        viewModelScope.launch {
            try {
                val newRole = !user.isAdmin
                usersCollection.document(user.uid).update("isAdmin", newRole).await()
                val roleTitle = if (newRole) "Quản trị viên (Admin)" else "Người dùng thường"
                _message.value = "Đã cập nhật vai trò ${user.fullName} thành $roleTitle"
            } catch (e: Exception) {
                _message.value = "Lỗi phân quyền: ${e.message}"
            } finally {
                userToToggleRole.value = null
            }
        }
    }

    private fun loadAllReports() {
        reportsListener = reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.appmuabandocu.model.Report>()
                snapshot.children.forEach { child ->
                    val report = child.getValue(com.example.appmuabandocu.model.Report::class.java)
                    report?.let { list.add(it) }
                }
                _reportList.clear()
                _reportList.addAll(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminViewModel", "Error loading reports: ${error.message}")
            }
        })
    }

    fun dismissReport(report: com.example.appmuabandocu.model.Report) {
        viewModelScope.launch {
            try {
                if (report.id.isNotEmpty()) {
                    reportsRef.child(report.id).removeValue().await()
                    _message.value = "Đã bỏ qua báo cáo"
                }
            } catch (e: Exception) {
                _message.value = "Lỗi xử lý báo cáo: ${e.message}"
            } finally {
                reportToProcess.value = null
            }
        }
    }

    fun deleteReportedProduct(report: com.example.appmuabandocu.model.Report) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (report.productId.isNotEmpty()) {
                    realtimeDb.child(report.productId).removeValue().await()
                    try {
                        productsCollection.document(report.productId).delete().await()
                    } catch (_: Exception) {}
                }
                // Mark report as resolved / remove
                if (report.id.isNotEmpty()) {
                    reportsRef.child(report.id).removeValue().await()
                }
                _message.value = "Đã xóa bài viết vi phạm bị báo cáo: \"${report.productName}\""
            } catch (e: Exception) {
                _message.value = "Lỗi xóa bài: ${e.message}"
            } finally {
                _isLoading.value = false
                reportToProcess.value = null
            }
        }
    }

    // Khóa tài khoản thành viên có thời hạn hoặc cảnh cáo
    // durationDays: 0 = Cảnh cáo, 3 = 3 ngày, 7 = 7 ngày, -1 = Vĩnh viễn
    fun banUserWithDuration(user: User, reason: String, durationDays: Int) {
        val userId = user.uid
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val now = System.currentTimeMillis()
                val bannedUntil = when (durationDays) {
                    0 -> 0L // Cảnh cáo, không đổi trạng thái khóa
                    3 -> now + 3L * 24L * 60L * 60L * 1000L
                    7 -> now + 7L * 24L * 60L * 60L * 1000L
                    else -> -1L // Khóa vĩnh viễn
                }

                if (durationDays == 0) {
                    // Chỉ gửi cảnh cáo
                    _message.value = "Đã gửi cảnh cáo vi phạm đến tài khoản \"${user.fullName.ifEmpty { user.email }}\"!"
                    try {
                        val notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId).push()
                        val notif = mapOf(
                            "id" to (notifRef.key ?: ""),
                            "userId" to userId,
                            "title" to "Cảnh cáo vi phạm quy chuẩn cộng đồng",
                            "message" to "Tài khoản của bạn nhận cảnh cáo từ Ban Quản Trị vì lý do: \"$reason\". Vui lòng tuân thủ quy định để không bị khóa tài khoản.",
                            "type" to "WARNING",
                            "timestamp" to now,
                            "isRead" to false
                        )
                        notifRef.setValue(notif)
                    } catch (_: Exception) {}
                } else {
                    val updates = mapOf<String, Any>(
                        "isBanned" to true,
                        "banReason" to reason,
                        "bannedUntil" to bannedUntil
                    )
                    usersCollection.document(userId).update(updates).await()
                    val durationLabel = when (durationDays) {
                        3 -> "3 ngày"
                        7 -> "7 ngày"
                        else -> "vĩnh viễn"
                    }
                    _message.value = "Đã khóa tài khoản \"${user.fullName.ifEmpty { user.email }}\" trong thời hạn $durationLabel!"

                    try {
                        val notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId).push()
                        val banMsg = if (durationDays == -1) {
                            "Tài khoản của bạn đã bị Ban Quản Trị khóa vĩnh viễn do vi phạm: \"$reason\"."
                        } else {
                            val expDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bannedUntil))
                            "Tài khoản của bạn tạm thời bị khóa $durationLabel (đến ngày $expDate) do vi phạm: \"$reason\"."
                        }
                        val notif = mapOf(
                            "id" to (notifRef.key ?: ""),
                            "userId" to userId,
                            "title" to "Tài khoản của bạn đã bị khóa",
                            "message" to banMsg,
                            "type" to "BAN",
                            "timestamp" to now,
                            "isRead" to false
                        )
                        notifRef.setValue(notif)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _message.value = "Lỗi khi xử lý kỷ luật tài khoản: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Khóa tài khoản thành viên (vĩnh viễn mặc định)
    fun banUser(user: User, reason: String) {
        banUserWithDuration(user, reason, -1)
    }

    // Mở khóa tài khoản thành viên
    fun unbanUser(user: User) {
        val userId = user.uid
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updates = mapOf<String, Any>(
                    "isBanned" to false,
                    "banReason" to "",
                    "bannedUntil" to 0L
                )
                usersCollection.document(userId).update(updates).await()
                _message.value = "Đã mở khóa tài khoản cho \"${user.fullName.ifEmpty { user.email }}\" thành công!"

                try {
                    val notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId).push()
                    val notif = mapOf(
                        "id" to (notifRef.key ?: ""),
                        "userId" to userId,
                        "title" to "Tài khoản của bạn đã được mở khóa",
                        "message" to "Ban Quản Trị đã mở khóa tài khoản của bạn. Bạn có thể tiếp tục đăng bài và mua bán trên Chợ Đồ Cũ.",
                        "type" to "SYSTEM",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )
                    notifRef.setValue(notif)
                } catch (_: Exception) {}
            } catch (e: Exception) {
                _message.value = "Lỗi khi mở khóa tài khoản: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Quản lý Banner động
    private fun loadAllBanners() {
        bannersListener = bannersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerItem>()
                snapshot.children.forEach { child ->
                    child.getValue(BannerItem::class.java)?.let { list.add(it) }
                }
                _bannerList.clear()
                _bannerList.addAll(list.sortedByDescending { it.createdAt })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminViewModel", "Error loading banners: ${error.message}")
            }
        })
    }

    fun addBanner(title: String, subtitle: String, imageUrl: String, linkTarget: String) {
        val key = bannersRef.push().key ?: return
        val banner = BannerItem(
            id = key,
            title = title.trim(),
            subtitle = subtitle.trim(),
            imageUrl = imageUrl.trim(),
            linkTarget = linkTarget.trim(),
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        bannersRef.child(key).setValue(banner)
            .addOnSuccessListener { _message.value = "Đã thêm banner quảng cáo mới thành công!" }
            .addOnFailureListener { _message.value = "Lỗi thêm banner: ${it.message}" }
    }

    fun toggleBannerActive(banner: BannerItem) {
        if (banner.id.isEmpty()) return
        val updated = banner.copy(isActive = !banner.isActive)
        bannersRef.child(banner.id).setValue(updated)
            .addOnSuccessListener {
                _message.value = if (updated.isActive) "Đã bật hiển thị banner" else "Đã tắt hiển thị banner"
            }
            .addOnFailureListener { _message.value = "Lỗi cập nhật banner: ${it.message}" }
    }

    fun deleteBanner(banner: BannerItem) {
        if (banner.id.isEmpty()) return
        bannersRef.child(banner.id).removeValue()
            .addOnSuccessListener { _message.value = "Đã xóa banner thành công!" }
            .addOnFailureListener { _message.value = "Lỗi xóa banner: ${it.message}" }
    }

    // Quản lý Danh mục trực tuyến
    private fun loadAllCategories() {
        categoriesListener = categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryItem>()
                snapshot.children.forEach { child ->
                    child.getValue(CategoryItem::class.java)?.let { list.add(it) }
                }
                _categoryList.clear()
                _categoryList.addAll(list.sortedByDescending { it.createdAt })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminViewModel", "Error loading categories: ${error.message}")
            }
        })
    }

    fun addCategory(name: String, iconName: String = "ic_all") {
        if (name.isBlank()) {
            _message.value = "Tên danh mục không được để trống"
            return
        }
        val key = categoriesRef.push().key ?: return
        val cat = CategoryItem(
            id = key,
            name = name.trim(),
            iconName = iconName,
            createdAt = System.currentTimeMillis()
        )
        categoriesRef.child(key).setValue(cat)
            .addOnSuccessListener { _message.value = "Đã thêm danh mục mới: \"$name\" thành công!" }
            .addOnFailureListener { _message.value = "Lỗi thêm danh mục: ${it.message}" }
    }

    fun deleteCategory(category: CategoryItem) {
        if (category.id.isEmpty()) return
        categoriesRef.child(category.id).removeValue()
            .addOnSuccessListener { _message.value = "Đã xóa danh mục: \"${category.name}\"" }
            .addOnFailureListener { _message.value = "Lỗi xóa danh mục: ${it.message}" }
    }

    companion object {
        fun sendReport(
            productId: String,
            productName: String,
            productImage: String,
            sellerId: String,
            sellerName: String,
            reason: String,
            details: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val reporterId = auth.currentUser?.uid ?: "anonymous"
            val reporterName = auth.currentUser?.displayName ?: "Người dùng ẩn danh"
            val reportsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("reports")
            val reportId = reportsRef.push().key ?: return

            val report = com.example.appmuabandocu.model.Report(
                id = reportId,
                productId = productId,
                productName = productName,
                productImage = productImage,
                sellerId = sellerId,
                sellerName = sellerName,
                reporterId = reporterId,
                reporterName = reporterName,
                reason = reason,
                details = details,
                timestamp = java.util.Date().time,
                status = "pending"
            )

            reportsRef.child(reportId).setValue(report)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Lỗi gửi báo cáo") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        productsListener?.let { realtimeDb.removeEventListener(it) }
        reportsListener?.let { reportsRef.removeEventListener(it) }
        bannersListener?.let { bannersRef.removeEventListener(it) }
        categoriesListener?.let { categoriesRef.removeEventListener(it) }
        usersListener?.remove()
    }
}
