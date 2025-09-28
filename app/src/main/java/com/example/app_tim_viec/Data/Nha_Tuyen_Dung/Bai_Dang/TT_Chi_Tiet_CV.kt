package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang

data class TT_Chi_Tiet_CV(
    var diaChiNoiLamViec: String = "",
    var tinhThanhPho: String = "",
    var soDienThoaiTuyenDung: String = "",
    var bangCapToiThieu: String = "",
    var chucVu: String = "",
    var soNamKinhNghiem: Int = 0,
    var mucLuong: String = "",
    var soLuongCanTuyen: Int = 0,
    var tinhChatCongViec: String = "",
    var kyNangChuyenMon: List<String> = listOf(),
    var chungChiYeuCau: List<String> = listOf(),
    var yeuCauGioiTinh: String? = null,

    // ⚡ Thay Pair<Int,Int> bằng object riêng để Firestore map được
    var yeuCauDoTuoi: DoTuoi? = null,

    var ngonNguYeuCau: List<String> = listOf(),
    var thoiGianLamViec: String = "",
    var gioLamViec: String = ""
)

// class con để map { min: 22, max: 30 }
data class DoTuoi(
    var min: Int = 0,
    var max: Int = 0
)
