package com.example.appmuabandocu.api

import com.example.appmuabandocu.data.District
import com.example.appmuabandocu.data.Province
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/?depth=2")
    suspend fun getProvinces(): List<Province>

    // Thêm hàm này để gọi /api/p/{provinceCode}?depth=2
    @GET("api/p/{provinceCode}?depth=2")
    suspend fun getProvinceByCode(@Path("provinceCode") provinceCode: String): Province

    // Thêm hàm này để gọi /api/d/{districtCode}?depth=2
    @GET("api/d/{districtCode}?depth=2")
    suspend fun getDistrictByCode(@Path("districtCode") districtCode: String): District
}
