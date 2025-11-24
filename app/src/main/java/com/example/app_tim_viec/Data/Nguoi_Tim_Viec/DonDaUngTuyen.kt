package com.example.app_tim_viec.Data.Nguoi_Tim_Viec

import java.io.Serializable

data class DonDaUngTuyen(
    var idDon: String = "",
    var idBaiDang: String = "",
    var tieuDeBaiDang: String = "",
    var tenCongTy: String = "",
    var trangThai: String = "", // Chờ duyệt, Đã duyệt...
    var mucLuong: String = "",
    var logoCongTy: String = ""
) : Serializable