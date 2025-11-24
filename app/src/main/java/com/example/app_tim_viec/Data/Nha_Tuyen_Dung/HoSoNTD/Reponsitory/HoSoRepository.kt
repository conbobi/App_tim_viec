package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD.Reponsitory

import android.util.Log
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD.CongTy
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.HoSoNTD.NhaTuyenDung
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HoSoRepository {
    private val db = FirebaseFirestore.getInstance()

    // Hàm suspend để chạy bất đồng bộ
    suspend fun getThongTinNTD(uid: String): NhaTuyenDung? {
        return try {
            // 1. Lấy thông tin Nhà Tuyển Dụng
            val documentSnapshot = db.collection("HoSoNTD").document(uid).get().await()

            if (documentSnapshot.exists()) {
                val ntd = documentSnapshot.toObject(NhaTuyenDung::class.java)

                // 2. Lấy thông tin Công Ty (nằm trong sub-collection)
                // Lưu ý: Query này lấy document đầu tiên tìm thấy trong sub-collection CongTy
                val congTySnapshot = db.collection("HoSoNTD").document(uid)
                    .collection("CongTy").get().await()

                if (!congTySnapshot.isEmpty) {
                    val congTyDoc = congTySnapshot.documents[0] // Lấy doc đầu tiên
                    val congTy = congTyDoc.toObject(CongTy::class.java)
                    ntd?.CongTy = congTy // Gán vào đối tượng cha
                }
                ntd
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("HoSoRepository", "Lỗi lấy dữ liệu: ${e.message}")
            null
        }
    }
}