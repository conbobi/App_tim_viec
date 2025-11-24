package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class DonUngTuyen(
    @JvmField
    var id: String = "",

    @get:PropertyName("trang_thai")
    @set:PropertyName("trang_thai")
    var trangThai: String = TrangThaiDon.CHUA_XU_LY.value,

    @get:PropertyName("ngay_nop")
    @set:PropertyName("ngay_nop")
    var ngayNop: Timestamp? = null,

    @get:PropertyName("ID_BaiDang")
    @set:PropertyName("ID_BaiDang")
    var idBaiDang: String = "",

    @get:PropertyName("ID_NTD")
    @set:PropertyName("ID_NTD")
    var idNTD: String = "",

    @get:PropertyName("ID_NguoiTV")
    @set:PropertyName("ID_NguoiTV")
    var idNguoiTV: String = "",

    @get:PropertyName("url_file_CV")
    @set:PropertyName("url_file_CV")
    var urlFileCV: String = "",

    // --- TRƯỜNG MỚI ĐƯỢC THÊM ---
    @get:PropertyName("Tieu_De") @set:PropertyName("Tieu_De") var tieuDe: String = "",

    // 👇 THÊM CÁC DÒNG NÀY VÀO CUỐI (TRƯỚC DẤU ĐÓNG })
    @get:Exclude @set:Exclude var tenNguoiTV: String? = null,
    @get:Exclude @set:Exclude var emailNguoiTV: String? = null,
    @get:Exclude @set:Exclude var sdtNguoiTV: String? = null
) {
    // Thêm constructor rỗng (cập nhật với 8 trường)
    constructor() : this("", TrangThaiDon.CHUA_XU_LY.value, null, "", "", "", "", "")
}