package com.example.appmuabandocu.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

suspend fun uploadImageToCloudinary(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload", ".jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", "unsigned_upload") // Thay bằng preset thật
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/drd9duful/image/upload") // Thay bằng cloud name thật
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = response.body?.string()
                val url = Regex("\"url\":\"(.*?)\"").find(json ?: "")?.groupValues?.get(1)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Đăng ảnh thành công!", Toast.LENGTH_SHORT).show()
                }
                url
            } else {
                Log.e("Cloudinary", "Upload failed: ${response.code}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi khi đăng ảnh!", Toast.LENGTH_SHORT).show()
                }
                null
            }
        } catch (e: Exception) {
            Log.e("Cloudinary", "Exception: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi khi đăng ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }
}