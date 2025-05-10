package com.example.appmuabandocu.feature_add_product

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyInputTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        // Xóa dấu chấm và ký tự "đ"
        val cleanText = originalText.replace(".", "").replace("đ", "").replace(" ", "")

        // Format lại thành tiền tệ
        val formatted = try {
            val number = cleanText.toLong()
            NumberFormat.getInstance(Locale("vi", "VN")).format(number)
        } catch (e: NumberFormatException) {
            ""
        }

        // Kết quả hiển thị là: ###.### đ
        val result = "$formatted đ"

        // Dùng OffsetMapping rỗng để tránh crash
        return TransformedText(
            AnnotatedString(result),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = result.length
                override fun transformedToOriginal(offset: Int): Int = cleanText.length
            }
        )
    }
}
