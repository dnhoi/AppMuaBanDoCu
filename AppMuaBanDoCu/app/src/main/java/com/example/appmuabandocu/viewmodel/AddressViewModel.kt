package com.example.appmuabandocu.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.api.ApiClient
import com.example.appmuabandocu.data.District
import com.example.appmuabandocu.data.Province
import com.example.appmuabandocu.model.User
import com.example.appmuabandocu.data.Ward
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddressViewModel: ViewModel(){
    private val apiService = ApiClient.provinceService

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users")

    private val _provinces = MutableStateFlow<List<Province>>(emptyList())
    val provinces: StateFlow<List<Province>> = _provinces

    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts: StateFlow<List<District>> = _districts

    private val _wards = MutableStateFlow<List<Ward>>(emptyList())
    val wards: StateFlow<List<Ward>> = _wards

    // Khởi tạo ViewModel và load danh sách tỉnh
    private val _selectedProvince = MutableStateFlow<Province?>(null)
    val selectedProvince: StateFlow<Province?> = _selectedProvince.asStateFlow()

    private val _selectedDistrict = MutableStateFlow<District?>(null)
    val selectedDistrict: StateFlow<District?> = _selectedDistrict.asStateFlow()

    private val _selectedWard = MutableStateFlow<Ward?>(null)
    val selectedWard: StateFlow<Ward?> = _selectedWard.asStateFlow()

    private val _streetAddress = MutableStateFlow("")
    val streetAddress: StateFlow<String> = _streetAddress.asStateFlow()

    // StateFlow cho thông tin người dùng
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // StateFlow cho địa chỉ đầy đủ
    private val _fullAddress = MutableStateFlow("")
    val fullAddress: StateFlow<String> = _fullAddress.asStateFlow()


    // Phương thức để cập nhật địa chỉ đường phố
    fun setStreetAddress(address: String) {
        _streetAddress.value = address
    }

    // Phương thức để chọn tỉnh
    fun selectProvince(province: Province) {
        _selectedProvince.value = province
        loadDistricts(province.code.toString())
    }

    // Phương thức để chọn quận/huyện
    fun selectDistrict(district: District) {
        _selectedDistrict.value = district
        loadWards(district.code.toString())
    }

    // Phương thức để chọn xã/phường
    fun selectWard(ward: Ward) {
        _selectedWard.value = ward
    }

    init {
        loadProvinces() // Load danh sách tỉnh khi ViewModel khởi tạo
    }
    // Load tỉnh
    fun loadProvinces() {
        viewModelScope.launch {
            try {
                val response = apiService.getProvinces()
                _provinces.value = response
                if (response.isNotEmpty()) {
                    loadDistricts(response[0].code.toString()) // Tự động load quận của tỉnh đầu tiên
                } else {
                    Log.e("ViewModel", "Danh sách tỉnh trống")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Lỗi load tỉnh: ${e.message}")
            }
        }
    }

    // Load quận/huyện bằng API riêng (depth=2 cho province cụ thể)
    fun loadDistricts(provinceCode: String) {
        viewModelScope.launch {
            try {
                val province = apiService.getProvinceByCode(provinceCode) // API: /api/p/{provinceCode}?depth=2
                _districts.value = province.districts ?: emptyList()

                if (_districts.value.isNotEmpty()) {
                    loadWards(_districts.value[0].code.toString())
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Lỗi load huyện: ${e.message}")
            }
        }
    }

    // Load xã/phường bằng API riêng (depth=2 cho district cụ thể)
    fun loadWards(districtCode: String) {
        viewModelScope.launch {
            try {
                val district = apiService.getDistrictByCode(districtCode) // API: /api/d/{districtCode}?depth=2
                _wards.value = district.wards ?: emptyList()
            } catch (e: Exception) {
                Log.e("ViewModel", "Lỗi load xã: ${e.message}")
            }
        }
    }

}