package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang
import java.io.Serializable // Nhớ import cái này
data class TT_Co_Ban(
    var Tieu_De: String = "",
    var Mo_Ta: String = "",
    var Yeu_Cau_CV: String = "",
    var Quyen_Loi: String = "",
    var Email_Lien_He: String = ""
): Serializable // 🔥 THÊM VÀO ĐÂY
