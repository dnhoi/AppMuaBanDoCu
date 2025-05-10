package com.example.appmuabandocu.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.R
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel: ViewModel() {
    private val authRepository = AuthRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()


    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    // Thêm state mới để theo dõi trạng thái đăng ký
    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    // Hàm để reset trạng thái đăng ký sau khi đã điều hướng
    fun resetRegisterSuccess() {
        _registerSuccess.value = false
    }

    fun setEmail(email: String) {
        _email.value = email
    }
    fun setPassword(password: String) {
        _password.value = password
    }

    fun setFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun setPhoneNumber(phoneNumber: String) {
        _phoneNumber.value = phoneNumber
    }


    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun getCurrentUser(): String? {
        return authRepository.getCurrentUserId()
    }

    private val _banDialogMessage = MutableStateFlow<String?>(null)
    val banDialogMessage: StateFlow<String?> = _banDialogMessage.asStateFlow()

    fun clearBanDialogMessage() {
        _banDialogMessage.value = null
    }

    private suspend fun checkUserBanStatus(userId: String, context: Context): Boolean {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val isBanned = doc.getBoolean("isBanned") ?: false
                val banReason = doc.getString("banReason") ?: "Vi phạm quy định cộng đồng"
                val bannedUntil = (doc.get("bannedUntil") as? Number)?.toLong() ?: 0L
                val now = System.currentTimeMillis()

                if (isBanned) {
                    if (bannedUntil > 0 && now < bannedUntil) {
                        val expDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bannedUntil))
                        signOut(context)
                        _banDialogMessage.value = "Tài khoản của bạn tạm thời bị khóa đến ngày $expDate vì lý do: \"$banReason\"."
                        return false
                    } else if (bannedUntil == -1L) {
                        signOut(context)
                        _banDialogMessage.value = "Tài khoản của bạn đã bị khóa vĩnh viễn vì lý do: \"$banReason\"."
                        return false
                    } else if (bannedUntil > 0 && now >= bannedUntil) {
                        // Tự động mở khóa khi đã hết hạn
                        firestore.collection("users").document(userId).update(
                            mapOf("isBanned" to false, "banReason" to "", "bannedUntil" to 0L)
                        ).await()
                    }
                }
            }
        } catch (_: Exception) {}
        return true
    }

    fun signInWithEmailPassword(context: Context) {
        if (email.value.isEmpty() || password.value.isEmpty()) {
            Toast.makeText(context, "Email và mật khẩu không được để trống", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                authRepository.signInWithEmailPassword(email.value, password.value)
                val userId = authRepository.getCurrentUserId()
                if (userId != null && !checkUserBanStatus(userId, context)) {
                    _isLoggedIn.value = false
                    return@launch
                }
                _isLoggedIn.value = true
                Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _isLoggedIn.value = false
                Toast.makeText(context, "Đăng nhập thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun registerWithEmailPassword(context: Context, address: String, province: String, district: String, ward: String) {
        if (email.value.isEmpty() || password.value.isEmpty() || fullName.value.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                // Đăng ký với Firebase Authentication
                authRepository.registerWithEmailPassword(email.value, password.value)

                // Lấy userId mới đăng ký
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    // Tạo dữ liệu người dùng
                    val userInfo = User(
                        uid = userId,
                        fullName = fullName.value,
                        email = email.value,
                        phoneNumber = phoneNumber.value,
                        address = address,
                        province = province,
                        district = district,
                        ward = ward,
                    )
                    // Lưu vào Firestore và cập nhật displayName
                    authRepository.updateUserProfile(userInfo)
                    _registerSuccess.value = true
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Đăng ký thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getGoogleSignInOptions(context: Context): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.web_client_id))
            .build()
    }

    fun signInWithGoogle(account: GoogleSignInAccount?, context: Context) {
        viewModelScope.launch {
            try {
                if (account == null) {
                    Toast.makeText(context, "Đăng nhập Google thất bại: Tài khoản không hợp lệ", Toast.LENGTH_SHORT).show()
                    throw Exception("Google sign-in failed: Account is null")
                }
                val idToken = account.idToken ?: throw Exception("Google sign-in failed: ID token is null")
                authRepository.signWithGoogle(idToken)
                val userId = authRepository.getCurrentUserId()
                if (userId != null && !checkUserBanStatus(userId, context)) {
                    _isLoggedIn.value = false
                    return@launch
                }
                _isLoggedIn.value = true
                Toast.makeText(context, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                _isLoggedIn.value = false
                Toast.makeText(context, "Đăng nhập Google thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut(context: Context) {
        authRepository.signOut()

        val googleSignInOptions = getGoogleSignInOptions(context)
        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        googleSignInClient.signOut()

        _isLoggedIn.value = false
        Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
    }


}