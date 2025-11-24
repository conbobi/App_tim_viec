package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD
import java.io.Serializable // Nhớ import cái này
data class NhaTuyenDung(
    var MaNTD: String = "",
    var HoTenNtd: String = "",
    var Email: String = "",
    var SoDienThoai: String = "",
    var ChucVu: String = "",
    var URL_avarta: String = "",
    // Thêm trường để hứng dữ liệu công ty
    var CongTy: CongTy? = null
): Serializable