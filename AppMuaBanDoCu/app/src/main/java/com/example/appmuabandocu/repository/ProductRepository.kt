package com.example.appmuabandocu.repository

import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository pattern để xử lý dữ liệu sản phẩm từ Firebase Firestore
 */
class ProductRepository(
    private val firestore: FirebaseFirestore,
    private val networkObserver: NetworkConnectivityObserver
) {
    private val productsCollection = firestore.collection("products")

    // Cache để lưu trữ tạm thời các sản phẩm đã lấy
    private val productsCache = MutableStateFlow<List<Product>>(emptyList())

    /**
     * Lấy tất cả sản phẩm từ Firestore
     * @return Flow của danh sách sản phẩm
     */
    fun getAllProducts(): Flow<List<Product>> {
        // Thiết lập listener để lắng nghe thay đổi từ Firestore
        productsCollection
            .whereEqualTo("displayed", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val products = snapshot.documents.mapNotNull { doc ->
                    val product = doc.toObject<Product>()
                    product?.copy(id = doc.id)
                }

                productsCache.update { products }
            }

        return productsCache
    }

    /**
     * Lấy chi tiết một sản phẩm theo ID
     * @param productId ID của sản phẩm
     * @return Sản phẩm nếu tìm thấy, null nếu không tìm thấy
     */
    suspend fun getProductById(productId: String): Product? {
        // Trước tiên, kiểm tra xem sản phẩm có trong cache không
        val cachedProduct = productsCache.value.find { it.id == productId }
        if (cachedProduct != null) {
            return cachedProduct
        }

        // Nếu không có kết nối mạng, không thể lấy sản phẩm mới
        if (!networkObserver.isNetworkAvailable.first()) {
            return null
        }

        // Truy vấn Firestore để lấy sản phẩm
        return try {
            val productDoc = productsCollection.document(productId).get().await()
            val product = productDoc.toObject<Product>()

            product?.copy(id = productId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Tạo sản phẩm mới trên Firebase
     */
    suspend fun createProduct(product: Product): String {
        val timestamp = Date().time
        val productId = if (product.id.isBlank()) productsCollection.document().id else product.id

        val newProduct = product.copy(
            id = productId,
            timestamp = timestamp
        )

        try {
            productsCollection.document(productId).set(newProduct).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e  // Re-throw để người gọi biết đã xảy ra lỗi
        }

        return productId
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    suspend fun updateProduct(product: Product) {
        if (!networkObserver.isNetworkAvailable.first()) {
            throw Exception("Không có kết nối mạng")
        }

        try {
            productsCollection.document(product.id).set(product).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e  // Re-throw để người gọi biết đã xảy ra lỗi
        }
    }

    /**
     * Xóa sản phẩm
     */
    suspend fun deleteProduct(productId: String) {
        if (!networkObserver.isNetworkAvailable.first()) {
            throw Exception("Không có kết nối mạng")
        }

        try {
            productsCollection.document(productId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e  // Re-throw để người gọi biết đã xảy ra lỗi
        }
    }

    /**
     * Lấy tất cả sản phẩm của một người dùng cụ thể
     * @param userId ID của người dùng
     * @return Danh sách sản phẩm của người dùng đó
     */
    suspend fun getProductsByUserId(userId: String): List<Product> {
        if (!networkObserver.isNetworkAvailable.first()) {
            throw Exception("Không có kết nối mạng")
        }

        return try {
            val querySnapshot = productsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                val product = doc.toObject<Product>()
                product?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
