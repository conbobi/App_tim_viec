package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory

import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BaiDangRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("BaiDangCV")

    // 🟢 Lấy toàn bộ bài đăng
    suspend fun getAllBaiDang(): List<Bai_Dang_CV> {
        return try {
            val snapshot = collection.get().await()
            val jobs = mutableListOf<Bai_Dang_CV>()

            for (doc in snapshot.documents) {
                val subSnapshot = doc.reference.collection("thongTinCoBan").get().await()
                if (!subSnapshot.isEmpty) {
                    val subDoc = subSnapshot.documents.first()

                    val baiDang = Bai_Dang_CV(
                        maBaiDang = doc.id,
                        maNhaTuyenDung = doc.getString("maNhaTuyenDung") ?: "",
                        thoiGianTao = doc.getTimestamp("thoiGianTao")?.toDate()?.time ?:0L,
                        thoiGianHetHan = doc.getTimestamp("thoiGianHetHan")?.toDate()?.time ?:0L,
                        tieuDe = subDoc.getString("Tieu_De") ?: "",
                        moTa = subDoc.getString("Mo_Ta") ?: "",
                        yeuCauCV = subDoc.getString("Yeu_Cau_CV") ?: "",
                        quyenLoi = subDoc.getString("Quyen_Loi") ?: "",
                        emailLienHe = subDoc.getString("Email_Lien_He") ?: ""
                    )

                    jobs.add(baiDang)
                }
            }

            jobs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    // 🟢 Lấy bài đăng theo ID
    suspend fun getBaiDangById(id: String): Bai_Dang_CV? {
        return try {
            val snapshot = collection.document(id).get().await()
            snapshot.toObject(Bai_Dang_CV::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🟢 Thêm hoặc cập nhật bài đăng
    suspend fun upsertBaiDang(baiDang: Bai_Dang_CV) {
        try {
            collection.document(baiDang.maBaiDang).set(baiDang).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🟢 Xóa bài đăng
    suspend fun deleteBaiDang(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
