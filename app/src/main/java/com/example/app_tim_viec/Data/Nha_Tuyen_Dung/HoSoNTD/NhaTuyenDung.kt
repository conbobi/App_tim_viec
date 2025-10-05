package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD.CongTy
data class NhaTuyenDung(
    var MaNTD: String="",
    var HoTenNtd: String="",
    var Email: String="",
    var SoDienThoai: String ="",
    var ChucVu: String ="",
    var URL_avarta: String="",
    var CongTy: CongTy ? =null
)
