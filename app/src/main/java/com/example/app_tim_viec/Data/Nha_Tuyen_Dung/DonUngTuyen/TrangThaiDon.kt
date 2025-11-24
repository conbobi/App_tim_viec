package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen

// Dùng enum để quản lý các trạng thái một cách an toàn
enum class TrangThaiDon(val value: String) {
    CHUA_XU_LY("chưa duyệt"), // Đây là trạng thái mặc định từ JSON của bạn
    DA_DUYET("đã duyệt"),
    TU_CHOI("từ chối");

    // Hàm này giúp chuyển đổi từ String trong Firebase về Enum
    companion object {
        fun fromString(value: String): TrangThaiDon {
            return values().find { it.value == value } ?: CHUA_XU_LY
        }
    }
}