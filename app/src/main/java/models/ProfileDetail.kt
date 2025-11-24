package com.example.app_tim_viec.models

import java.io.Serializable

// Class chứa toàn bộ thông tin chi tiết
data class ProfileDetail(
    var nganhNgheQuanTam: List<String> = ArrayList(),
    var trinhDoHocVan: String = "Chưa cập nhật",
    var mucLuongMongMuon: String = "Thương lượng",
    var hinhThucLamViec: String = "Toàn thời gian",
    var kyNangChuyenMon: List<String> = ArrayList(),
    var kinhNghiemLamViec: ArrayList<Experience> = ArrayList()
) : Serializable

// Class con cho từng mục kinh nghiệm
data class Experience(
    var id: String = System.currentTimeMillis().toString(), // ID để phân biệt khi sửa/xóa
    var chucVu: String = "",
    var congTy: String = "",
    var thoiGianBatDau: String = "",
    var thoiGianKetThuc: String = "",
    var moTa: String = ""
) : Serializable