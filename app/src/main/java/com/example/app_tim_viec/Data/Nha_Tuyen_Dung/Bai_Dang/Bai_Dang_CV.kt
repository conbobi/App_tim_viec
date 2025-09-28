package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang

data class Bai_Dang_CV(
    var maBaiDang: String = "",                 // ID bài đăng
    var maNhaTuyenDung: String = "",            // ID nhà tuyển dụng
    var thongTinCoBan: TT_Co_Ban? = null,       // Thông tin cơ bản
    var thongTinChiTiet: TT_Chi_Tiet_CV? = null,// Thông tin chi tiết
    var thoiGianTao: Long = 0,                  // Thời gian tạo
    var thoiGianHetHan: Long = 0,                // Thời gian hết hạn)
    // Các field của subcollection
    val tieuDe: String = "",
    val moTa: String = "",
    val yeuCauCV: String = "",
    val quyenLoi: String = "",
    val emailLienHe: String = ""
)
