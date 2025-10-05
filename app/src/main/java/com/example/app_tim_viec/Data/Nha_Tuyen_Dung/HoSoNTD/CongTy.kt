package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import  java.io.Serializable
data class CongTy (
    var MaCongTy : String="",
    var UrlAnh_CongTy : String="",
    var Mota : String="",
    var Email: String ="",
    var DS_BaiDang: List<Bai_Dang_CV> = listOf(),
    var SoDienTHoai: String=""
)

data class DiaChi(
    var  Tinh: String="",
    var  Huyen: String=""
)