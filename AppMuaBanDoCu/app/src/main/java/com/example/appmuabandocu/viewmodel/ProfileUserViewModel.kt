package com.example.appmuabandocu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.Product
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.repository.ProductRepository
import com.example.appmuabandocu.repository.UserRepository
import com.example.appmuabandocu.utils.NetworkConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileUserViewModel(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _userProducts = MutableStateFlow<List<Product>>(emptyList())
    val userProducts: StateFlow<List<Product>> = _userProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userData = userRepository.getUserById(userId)
                _user.value = userData

                if (userData == null) {
                    _errorMessage.value = "Không tìm thấy thông tin người dùng"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi tải thông tin người dùng: ${e.localizedMessage ?: "Lỗi không xác định"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProducts(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                android.util.Log.d("ProfileUserViewModel", "Đang lấy sản phẩm cho userId: $userId")

                val products = userRepository.getProductsByUserId(userId)
                android.util.Log.d("ProfileUserViewModel", "Đã lấy được ${products.size} sản phẩm")

                _userProducts.value = products.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                android.util.Log.e("ProfileUserViewModel", "Lỗi khi tải sản phẩm: ${e.message}")
                _errorMessage.value = "Lỗi khi tải sản phẩm: ${e.localizedMessage ?: "Lỗi không xác định"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileUserViewModel::class.java)) {
                    val firestore = FirebaseFirestore.getInstance()
                    val auth = FirebaseAuth.getInstance()
                    val networkObserver = NetworkConnectivityObserver(android.app.Application())

                    val userRepository = UserRepository(
                        firestore = firestore,
                        auth = auth,
                        networkObserver = networkObserver
                    )

                    val productRepository = ProductRepository(
                        firestore = firestore,
                        networkObserver = networkObserver
                    )

                    return ProfileUserViewModel(
                        userRepository = userRepository,
                        productRepository = productRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
