package com.example.appmuabandocu.viewmodel


import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.appmuabandocu.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.java


class ManageProductViewModel : ViewModel() {


    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")


    private val _productList = mutableStateListOf<Product>()
    val productList: List<Product> get() = _productList


    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)


    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message


    private val _productToDelete = mutableStateOf<Product?>(null)
    val productToDelete = _productToDelete


    init {
        loadUserProducts()
    }


    // Tải sản phẩm của người dùng từ Firebase
    fun loadUserProducts() {
        isLoading.value = true
        val userId = FirebaseAuth.getInstance().currentUser?.uid


        if (userId != null) {
            db.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newProducts = mutableListOf<Product>()
                    snapshot.children.forEach { childSnapshot ->
                        val product = childSnapshot.getValue(Product::class.java)
                        product?.let { newProducts.add(it) }
                    }
                    _productList.clear()
                    _productList.addAll(newProducts)
                    isLoading.value = false
                    Log.d("RealtimeDatabase", "Dữ liệu sản phẩm của người dùng đã được tải thành công!")
                }


                override fun onCancelled(error: DatabaseError) {
                    isLoading.value = false
                    errorMessage.value = "Lỗi khi tải dữ liệu: ${error.message}"
                    Log.e("RealtimeDatabase", "Lỗi tải dữ liệu: ", error.toException())
                }
            })
        } else {
            errorMessage.value = "Người dùng chưa đăng nhập!"
            Log.e("RealtimeDatabase", "Người dùng chưa đăng nhập!")
        }
    }


    // Cập nhật trạng thái hiển thị (ẩn/hiện) của sản phẩm
    fun toggleProductDisplay(product: Product) {
        val productId = product.id
        val updatedProduct = product.displayed?.let { product.copy(displayed = !it) }


        if (productId.isNotEmpty()) {
            db.child(productId).setValue(updatedProduct)
                .addOnSuccessListener {
                    Log.d("RealtimeDatabase", "Cập nhật hiển thị sản phẩm thành công!")
                    _message.value = "Cập nhật hiển thị sản phẩm thành công!"
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Lỗi cập nhật hiển thị sản phẩm: ${e.message}"
                    Log.e("RealtimeDatabase", "Lỗi cập nhật hiển thị sản phẩm: ", e)
                }
        }
    }
    // Gọi khi người dùng chọn xóa
    fun confirmDelete(product: Product) {
        _productToDelete.value = product
    }


    // Hủy xác nhận xóa
    fun cancelDelete() {
        _productToDelete.value = null
    }


    // Xác nhận xóa sản phẩm đã chọn
    fun performDelete() {
        _productToDelete.value?.let {
            deleteProduct(it)
        }
        _productToDelete.value = null
    }


    // Xóa sản phẩm khỏi Firebase
    fun deleteProduct(product: Product) {
        val productId = product.id


        if (productId.isNotEmpty()) {
            db.child(productId).removeValue()
                .addOnSuccessListener {
                    Log.d("RealtimeDatabase", "Xóa sản phẩm thành công!")
                    _message.value = "Xóa sản phẩm thành công!"
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Lỗi xóa sản phẩm: ${e.message}"
                    Log.e("RealtimeDatabase", "Lỗi xóa sản phẩm: ", e)
                }
        }
    }
    // Cập nhật trạng thái của sản phẩm (còn hàng, đã bán, ẩn)
    fun updateProductStatus(product: Product, newStatus: String) {
        val productId = product.id
        val updatedProduct = product.copy(status = newStatus)

        if (productId.isNotEmpty()) {
            isLoading.value = true
            db.child(productId).setValue(updatedProduct)
                .addOnSuccessListener {
                    Log.d("RealtimeDatabase", "Cập nhật trạng thái sản phẩm thành công!")
                    _message.value = "Đã cập nhật trạng thái sản phẩm thành công"
                    isLoading.value = false
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Lỗi cập nhật trạng thái sản phẩm: ${e.message}"
                    Log.e("RealtimeDatabase", "Lỗi cập nhật trạng thái sản phẩm: ", e)
                    isLoading.value = false
                }
        } else {
            errorMessage.value = "Không thể cập nhật sản phẩm với ID rỗng"
        }
    }

    // Đẩy tin / Làm mới bài đăng (Bump / Boost Post)
    fun bumpProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        val now = System.currentTimeMillis()
        val lastBump = product.lastBumpedAt
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (lastBump > 0 && (now - lastBump) < oneDayMillis) {
            val remaining = oneDayMillis - (now - lastBump)
            val hours = remaining / (60 * 60 * 1000L)
            val minutes = (remaining % (60 * 60 * 1000L)) / (60 * 1000L)
            onResult(false, "Mỗi tin chỉ được đẩy 1 lần / 24 giờ. Vui lòng quay lại sau ${hours}h ${minutes}m!")
            return
        }

        val productId = product.id
        if (productId.isEmpty()) {
            onResult(false, "Mã sản phẩm không hợp lệ!")
            return
        }

        val updates = mapOf<String, Any>(
            "timestamp" to now,
            "lastBumpedAt" to now
        )

        db.child(productId).updateChildren(updates)
            .addOnSuccessListener {
                _message.value = "Đã đẩy tin lên đầu trang chủ thành công!"
                // Gửi thông báo đến In-App Notification Center
                try {
                    val notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(product.userId).push()
                    val notif = mapOf(
                        "id" to (notifRef.key ?: ""),
                        "userId" to product.userId,
                        "title" to "Đẩy tin thành công!",
                        "message" to "Bài đăng \"${product.productName}\" của bạn đã được đưa lên vị trí đầu tiên trên Trang Chủ.",
                        "type" to "BUMP",
                        "timestamp" to now,
                        "isRead" to false,
                        "relatedId" to productId
                    )
                    notifRef.setValue(notif)
                } catch (_: Exception) {}
                onResult(true, "Đã đẩy tin lên đầu trang chủ thành công! Tin của bạn đang ở vị trí ưu tiên.")
            }
            .addOnFailureListener { e ->
                onResult(false, "Lỗi khi đẩy tin: ${e.message}")
            }
    }
}

