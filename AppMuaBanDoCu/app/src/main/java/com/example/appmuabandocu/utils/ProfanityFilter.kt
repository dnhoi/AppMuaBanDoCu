package com.example.appmuabandocu.utils

import java.text.Normalizer
import java.util.regex.Pattern

data class ProfanityCheckResult(
    val isValid: Boolean,
    val category: String = "",
    val detectedWord: String = "",
    val errorMessage: String = ""
)

object ProfanityFilter {

    // Danh sách mặt hàng cấm kinh doanh theo quy định
    private val PROHIBITED_ITEMS = listOf(
        "vu khi", "sung", "sung dan", "sung hoi", "sung ban hoa cai", "dan sung",
        "dao gam", "ma tau", "kiem nhat", "roi dien", "sung dien", "binh xit hoi cay",
        "ma tuy", "can sa", "co my", "thuoc phien", "kẹo ke", "bong cuoi", "shisha",
        "dong vat hoang da", "nga voi", "sung te giac", "cao ho", "vay te te",
        "tien gia", "bang gia", "cccd gia", "giay to gia", "chung chi gia",
        "phao no", "phao hoa no", "thuoc no", "kip no", "chat no",
        "thuoc kich duc", "kich duc", "khieu dam"
    )

    // Danh sách từ ngữ lừa đảo / cờ bạc nhạy cảm
    private val SCAM_KEYWORDS = listOf(
        "keo baccarat", "tai xiu", "danh bac", "ca do", "lo de", "so de",
        "nap tien lam nhiem vu", "kiem tien telegram", "hoa hong 50%", "hoa hong 100%",
        "hack game", "nhan code mien phi lua dao", "chuyen coc gap"
    )

    // Danh mục tài khoản ngân hàng hoặc số điện thoại lừa đảo đã ghi nhận trong blacklist
    private val BLACKLISTED_ACCOUNTS = listOf(
        "098765432100", // Mẫu số TK lừa đảo bị blacklist
        "1029384756",
        "999988887777"
    )

    // Danh sách từ ngữ thô tục, chửi thề
    private val PROFANITY_WORDS = listOf(
        "dm", "dcm", "vcl", "clgt", "lon", "cac", "buoi", "dit", "dit me",
        "cho de", "suc vat", "do cho", "dmm", "cl", "vl"
    )

    /**
     * Chuẩn hóa văn bản: Chuyển chữ thường, bỏ dấu tiếng Việt, loại bỏ ký tự đặc biệt lách luật (d.ụ, s-ú-n-g...)
     */
    fun normalizeText(input: String): String {
        var str = input.lowercase().trim()
        // Chuẩn hóa ký tự unicode
        str = Normalizer.normalize(str, Normalizer.Form.NFD)
        str = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(str).replaceAll("")
        // Thay ký tự đ / Đ
        str = str.replace("đ", "d").replace("Đ", "d")
        return str
    }

    /**
     * Kiểm tra toàn diện tiêu đề và nội dung bài viết
     */
    fun checkContent(title: String, description: String, price: String = ""): ProfanityCheckResult {
        val rawCombined = "$title $description $price"
        val normalized = normalizeText(rawCombined)
        // Dạng nén (loại bỏ toàn bộ khoảng trắng và dấu câu để chống lách từ: "s.ú.n.g" -> "sung")
        val compressed = normalized.replace(Regex("[^a-z0-9]"), "")

        // 1. Kiểm tra tài khoản trong danh sách đen
        for (acc in BLACKLISTED_ACCOUNTS) {
            if (compressed.contains(acc)) {
                return ProfanityCheckResult(
                    isValid = false,
                    category = "Số tài khoản danh sách đen",
                    detectedWord = acc,
                    errorMessage = "Phát hiện số tài khoản/số điện thoại ($acc) nằm trong danh sách đen cảnh báo lừa đảo!"
                )
            }
        }

        // 2. Kiểm tra mặt hàng cấm
        for (item in PROHIBITED_ITEMS) {
            val itemNorm = normalizeText(item)
            val itemCompressed = itemNorm.replace(" ", "")
            if (normalized.contains(Regex("\\b$itemNorm\\b")) || compressed.contains(itemCompressed)) {
                return ProfanityCheckResult(
                    isValid = false,
                    category = "Mặt hàng cấm buôn bán",
                    detectedWord = item,
                    errorMessage = "Sản phẩm chứa từ khóa bị cấm: \"$item\". Quy định nghiêm cấm buôn bán vũ khí, chất cấm, hàng giả, động vật quý hiếm!"
                )
            }
        }

        // 3. Kiểm tra từ ngữ lừa đảo / cờ bạc
        for (scam in SCAM_KEYWORDS) {
            val scamNorm = normalizeText(scam)
            val scamCompressed = scamNorm.replace(" ", "")
            if (normalized.contains(scamNorm) || compressed.contains(scamCompressed)) {
                return ProfanityCheckResult(
                    isValid = false,
                    category = "Hành vi cờ bạc / lừa đảo",
                    detectedWord = scam,
                    errorMessage = "Nội dung chứa từ khóa vi phạm tiêu chuẩn cộng đồng: \"$scam\"!"
                )
            }
        }

        // 4. Kiểm tra từ ngữ thô tục
        for (word in PROFANITY_WORDS) {
            val wordNorm = normalizeText(word)
            // Kiểm tra theo ranh giới từ để tránh trùng nhầm
            if (normalized.contains(Regex("\\b$wordNorm\\b"))) {
                return ProfanityCheckResult(
                    isValid = false,
                    category = "Ngôn từ thô tục",
                    detectedWord = word,
                    errorMessage = "Vui lòng giữ lịch sự và văn minh. Phát hiện ngôn từ không phù hợp: \"$word\"!"
                )
            }
        }

        return ProfanityCheckResult(isValid = true)
    }
}
