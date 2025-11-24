package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD.Reponsitory.HoSoRepository


class HoSoNTD_Reponsition {

    private val db = FirebaseFirestore.getInstance()

    suspend fun updateNhaTuyenDung(
        maNTD: String,
        hoTen: String,
        chucVu: String,
        email: String,
        soDienThoai: String,
        tenCongTy: String,
        diaChiCongTy: String,
        quyMo: String
    ): Boolean {
        return try {
            val data = mapOf(
                "HoTenNtd" to hoTen,
                "ChucVu" to chucVu,
                "Email" to email,
                "SoDienThoai" to soDienThoai,
                "TenCongTy" to tenCongTy,
                "DiaChiCongTy" to diaChiCongTy,
                "QuyMo" to quyMo
            )

            db.collection("NhaTuyenDung")
                .document(maNTD)
                .update(data)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



}