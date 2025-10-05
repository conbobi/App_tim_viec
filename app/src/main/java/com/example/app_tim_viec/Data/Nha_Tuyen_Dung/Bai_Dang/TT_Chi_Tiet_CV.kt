package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang
import java.io.Serializable
import android.R

data class TT_Chi_Tiet_CV(
    var diaChiNoiLamViec: String = "",
    var tinhThanhPho: String = "",
    var soDienThoaiTuyenDung: String = "",
    var bangCapToiThieu: String = "",
    var chucVu: String = "",
    var soNamKinhNghiem: Int = 0,
    var mucLuong: Int,
    var soLuongCanTuyen: Int = 0,
    var tinhChatCongViec: String = "",
    var kyNangChuyenMon: List<String> = listOf(),
    var chungChiYeuCau: List<String> = listOf(),
    var yeuCauGioiTinh: String? = null,

    // ⚡ phải khai báo đúng như thế này
    var yeuCauDoTuoi: DoTuoi? = null,

    var ngonNguYeuCau: String,
    var thoiGianLamViec: thoiGianLamViec,
    var gioLamViec: String = ""
): Serializable


data class DoTuoi(
    var min: Int = 0,
    var max: Int = 0
)
data class thoiGianLamViec(
    var tu: String="",
    var den: String=""
)

