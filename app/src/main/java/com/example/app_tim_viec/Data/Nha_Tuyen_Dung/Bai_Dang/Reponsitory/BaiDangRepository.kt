package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory

import android.R
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.DoTuoi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.TT_Chi_Tiet_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.thoiGianLamViec
import kotlin.String
import kotlin.collections.List

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
                val subSnapshotChiTiet = doc.reference.collection("thongTinChiTiet").get().await()

                var ttChiTiet: TT_Chi_Tiet_CV? = null
                var tieuDe = ""
                var moTa = ""
                var yeuCauCV = ""
                var quyenLoi = ""
                var emailLienHe = ""

                if (!subSnapshot.isEmpty) {
                    val subDoc = subSnapshot.documents.first()
                    tieuDe = subDoc.getString("Tieu_De") ?: ""
                    moTa = subDoc.getString("Mo_Ta") ?: ""
                    yeuCauCV = subDoc.getString("Yeu_Cau_CV") ?: ""
                    quyenLoi = subDoc.getString("Quyen_Loi") ?: ""
                    emailLienHe = subDoc.getString("Email_Lien_He") ?: ""
                }

                if (!subSnapshotChiTiet.isEmpty) {
                    val subDoc = subSnapshotChiTiet.documents.first()
                    ttChiTiet = TT_Chi_Tiet_CV(
                        soNamKinhNghiem = subDoc.getLong("soNamKinhNghiem")?.toInt() ?: 0,
                        yeuCauDoTuoi = DoTuoi(
                            min = (subDoc.get("yeuCauDoTuoi.min") as? Number)?.toInt() ?: 0,
                            max = (subDoc.get("yeuCauDoTuoi.max") as? Number)?.toInt() ?: 0
                        ),
                        thoiGianLamViec = thoiGianLamViec(
                            tu = (subDoc.get("thoiGianLamViec.tu") as? String) ?: "",
                            den = (subDoc.get("thoiGianLamViec.den") as? String) ?: ""
                        ),
                        bangCapToiThieu = subDoc.getString("bangCapToiThieu") ?: "",
                        yeuCauGioiTinh = subDoc.getString("yeuCauGioiTinh") ?: "",
                        soDienThoaiTuyenDung = subDoc.getString("soDienThoaiTuyenDung") ?: "",
                        chucVu = subDoc.getString("chucVu") ?: "",
                        kyNangChuyenMon = subDoc.get("kyNangChuyenMon") as? List<String> ?: emptyList(),
                        diaChiNoiLamViec = subDoc.getString("diaChiNoiLamViec") ?: "",
                        ngonNguYeuCau = subDoc.getString("ngonNguYeuCau") ?: "",
                        tinhThanhPho = subDoc.getString("tinhThanhPho") ?: "",
                        soLuongCanTuyen = subDoc.getLong("soLuongCanTuyen")?.toInt() ?: 0,
                        chungChiYeuCau = subDoc.get("chungChiYeuCau") as? List<String> ?: emptyList(),
                        mucLuong = subDoc.getLong("mucLuong")?.toInt() ?: 0
                    )
                }

                // ✅ Tạo đối tượng sau khi có đủ dữ liệu
                val baiDang = Bai_Dang_CV(
                    maBaiDang = doc.id,
                    maNhaTuyenDung = doc.getString("maNhaTuyenDung") ?: "",
                    thoiGianTao = doc.getTimestamp("thoiGianTao")?.toDate()?.time ?: 0L,
                    thoiGianHetHan = doc.getTimestamp("thoiGianHetHan")?.toDate()?.time ?: 0L,
                    thongTinChiTiet = ttChiTiet,
                    tieuDe = tieuDe,
                    moTa = moTa,
                    yeuCauCV = yeuCauCV,
                    quyenLoi = quyenLoi,
                    emailLienHe = emailLienHe
                )

                jobs.add(baiDang)
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
    suspend fun updateBaiDang(job: Bai_Dang_CV) {
        val jobRef = collection.document(job.maBaiDang)

        val thongTinCoBan = mapOf(
            "Tieu_De" to job.tieuDe,
            "Mo_Ta" to job.moTa,
            "Yeu_Cau_CV" to job.yeuCauCV,
            "Quyen_Loi" to job.quyenLoi,
            "Email_Lien_He" to job.emailLienHe
        )

        val thongTinChiTiet = job.thongTinChiTiet?.let {
            mapOf(
                "soNamKinhNghiem" to it.soNamKinhNghiem,
                "yeuCauDoTuoi.min" to it.yeuCauDoTuoi?.min,
                "yeuCauDoTuoi.max" to it.yeuCauDoTuoi?.max,
                "thoiGianLamViec.tu" to it.thoiGianLamViec.tu,
                "thoiGianLamViec.den" to it.thoiGianLamViec.den,
                "bangCapToiThieu" to it.bangCapToiThieu,
                "yeuCauGioiTinh" to it.yeuCauGioiTinh,
                "soDienThoaiTuyenDung" to it.soDienThoaiTuyenDung,
                "chucVu" to it.chucVu,
                "kyNangChuyenMon" to it.kyNangChuyenMon,
                "diaChiNoiLamViec" to it.diaChiNoiLamViec,
                "ngonNguYeuCau" to it.ngonNguYeuCau,
                "tinhThanhPho" to it.tinhThanhPho,
                "soLuongCanTuyen" to it.soLuongCanTuyen,
                "chungChiYeuCau" to it.chungChiYeuCau,
                "mucLuong" to it.mucLuong
            )
        }

        // Ghi dữ liệu Firestore
        jobRef.collection("thongTinCoBan").document("info").set(thongTinCoBan).await()
        thongTinChiTiet?.let {
            jobRef.collection("thongTinChiTiet").document("detail").set(it).await()
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
