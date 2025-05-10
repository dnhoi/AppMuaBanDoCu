package com.example.appmuabandocu.repository

import com.example.appmuabandocu.model.User
    import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlin.text.get

/**
 * Repository pattern để xử lý dữ liệu người dùng từ Firebase
 */
class UserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkObserver: NetworkConnectivityObserver
) {
    private val usersCollection = firestore.collection("users")
    private val realtimeDb = Firebase.database.reference.child("users")
    private val productsCollection = firestore.collection("products")

    /**
     * Lấy thông tin người dùng hiện tại đang đăng nhập
     * @return User nếu đã đăng nhập, null nếu chưa đăng nhập
     */
    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        return getUserById(currentUser.uid)
    }

    /**
     * Lấy thông tin người dùng theo ID
     * @param userId ID của người dùng cần lấy thông tin
     * @return User nếu tìm thấy, null nếu không tìm thấy
     */
    suspend fun getUserById(userId: String): User? {
        if (!networkObserver.isNetworkAvailable.first()) {
            android.util.Log.d("UserRepository", "Không có kết nối mạng khi tìm user: $userId")
            return null // Không có kết nối mạng
        }

        android.util.Log.d("UserRepository", "Đang tìm user với ID: $userId")

        // Kiểm tra xem userId có rỗng không
        if (userId.isEmpty()) {
            android.util.Log.d("UserRepository", "userId rỗng")
            return null
        }

        return try {
            // Lấy dữ liệu từ Firestore
            val userDoc = usersCollection.document(userId).get().await()

            if (userDoc.exists()) {
                val userData = userDoc.data
                android.util.Log.d("UserRepository", "Đã tìm thấy user trong Firestore: ${userData?.get("name") ?: "null"}")

                if (userData != null) {
                    // Tạo đối tượng User từ dữ liệu Firestore
                    val user = User(
                        uid = userId,
                        email = userData["email"] as? String ?: "",
                        fullName = userData["name"] as? String ?: "", // Sử dụng trường 'name' thay vì 'fullName'
                        avatarUrl = userData["avatarUrl"] as? String ?: "",
                        phoneNumber = userData["phoneNumber"] as? String,
                        address = userData["address"] as? String,
                        province = userData["province"] as? String,
                        district = userData["district"] as? String,
                        ward = userData["ward"] as? String
                    )

                    android.util.Log.d("UserRepository", "User được tạo: fullName=${user.fullName}, address=${user.address}")
                    return user
                }
            }

            // Nếu không tìm thấy trong Firestore, thử tìm trong sản phẩm
            android.util.Log.d("UserRepository", "Không tìm thấy user trong Firestore, thử lấy từ sản phẩm")
            return getUserInfoFromProduct(userId)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Lỗi khi lấy user từ Firestore: ${e.message}")
            // Nếu có lỗi, thử lấy thông tin từ sản phẩm
            return getUserInfoFromProduct(userId)
        }
    }

    suspend fun getProductsByUserId(userId: String): List<Product> {
        if (userId.isEmpty()) return emptyList()

        android.util.Log.d("UserRepository", "Bắt đầu lấy sản phẩm với userId: $userId")
        val productsList = mutableListOf<Product>()

        // 1. First try to get products from Firestore
        try {
            val result = productsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            android.util.Log.d("UserRepository", "Số lượng sản phẩm tìm thấy từ Firestore (userId): ${result.size()}")

            result.documents.forEach { doc ->
                try {
                    val product = doc.toObject(Product::class.java)
                    if (product != null) {
                        product.id = doc.id
                        productsList.add(product)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserRepository", "Lỗi chuyển đổi sản phẩm Firestore: ${e.message}")
                }
            }

            // Check idUser field in Firestore too
            val resultIdUser = productsCollection
                .whereEqualTo("idUser", userId)
                .get()
                .await()

            android.util.Log.d("UserRepository", "Số lượng sản phẩm tìm thấy từ Firestore (idUser): ${resultIdUser.size()}")

            resultIdUser.documents.forEach { doc ->
                try {
                    val product = doc.toObject(Product::class.java)
                    if (product != null && productsList.none { it.id == doc.id }) {
                        product.id = doc.id
                        productsList.add(product)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UserRepository", "Lỗi chuyển đổi sản phẩm: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Lỗi khi lấy sản phẩm từ Firestore: ${e.message}")
        }

        // 2. Now try to fetch from Realtime Database
        try {
            val realtimeDbProducts = Firebase.database.reference.child("products")
            android.util.Log.d("UserRepository", "Đang tìm sản phẩm từ Realtime Database với userId: $userId")

            val snapshot = realtimeDbProducts.get().await()
            var countFound = 0

            if (snapshot.exists()) {
                snapshot.children.forEach { child ->
                    try {
                        val productData = child.getValue<HashMap<String, Any>>()
                        if (productData != null) {
                            val productUserId = productData["userId"] as? String

                            if (productUserId == userId) {
                                countFound++
                                val product = Product(
                                    id = child.key ?: "",
                                    userId = productUserId,
                                    productName = productData["productName"] as? String ?: "",
                                    price = productData["price"] as? String ?: "0",
                                    category = productData["category"] as? String ?: "",
                                    address = productData["address"] as? String ?: "",
                                    imageUrl = productData["imageUrl"] as? String ?: "",
                                    userName = productData["userName"] as? String ?: "",
                                    userAvatar = productData["userAvatar"] as? String ?: "",
                                    numberUser = productData["numberUser"] as? String ?: "",
                                    status = productData["status"] as? String ?: "available",
                                    timestamp = (productData["timestamp"] as? Long) ?: 0L
                                )

                                if (productsList.none { it.id == product.id }) {
                                    productsList.add(product)
                                    android.util.Log.d("UserRepository", "Thêm sản phẩm từ RTDB: ${product.productName}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("UserRepository", "Lỗi xử lý sản phẩm từ RTDB: ${e.message}")
                    }
                }
            }
            android.util.Log.d("UserRepository", "Tìm thấy $countFound sản phẩm từ RTDB")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Lỗi truy vấn RTDB: ${e.message}")
        }

        android.util.Log.d("UserRepository", "Tổng số sản phẩm tìm thấy (cả Firestore và RTDB): ${productsList.size}")
        return productsList
    }

    /**
     * Cố gắng lấy thông tin cơ bản của người dùng từ sản phẩm gần đây nhất của họ
     */
    private suspend fun getUserInfoFromProduct(userId: String): User? {
        return try {
            val query = productsCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                android.util.Log.d("UserRepository", "Không tìm thấy sản phẩm nào của user: $userId")
                null
            } else {
                val product = query.documents.first().toObject<Product>()

                if (product != null) {
                    android.util.Log.d("UserRepository", "Đã tìm thấy thông tin trong sản phẩm: ${product.userName}")
                    // Tạo đối tượng user tối thiểu từ thông tin sản phẩm
                    User(
                        uid = userId,
                        fullName = product.userName,
                        avatarUrl = product.userAvatar,
                        phoneNumber = product.numberUser,
                        address = product.address,
                        province = product.provinceFB
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Lỗi khi lấy thông tin từ sản phẩm: ${e.message}")
            null
        }
    }

    /**
     * Cập nhật thông tin người dùng
     * @param user thông tin người dùng cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    suspend fun updateUserProfile(user: User): Boolean {
        if (!networkObserver.isNetworkAvailable.first()) {
            return false // Không có kết nối mạng
        }

        return try {
            usersCollection.document(user.uid).set(user.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Tìm kiếm người dùng theo tên
     * @param query từ khóa tìm kiếm
     * @return danh sách người dùng phù hợp
     */
    suspend fun searchUsers(query: String): List<User> {
        if (!networkObserver.isNetworkAvailable.first()) {
            return emptyList() // Không có kết nối mạng
        }

        return try {
            val searchResults = usersCollection
                .orderBy("fullName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            searchResults.documents.mapNotNull { doc ->
                doc.toObject<User>()?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
