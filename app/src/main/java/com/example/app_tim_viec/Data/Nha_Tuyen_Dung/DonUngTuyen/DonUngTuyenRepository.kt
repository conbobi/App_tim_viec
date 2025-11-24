package com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.android.gms.tasks.Tasks

class DonUngTuyenRepository {

    private val db = FirebaseFirestore.getInstance()
    private val donUngTuyenCollection = db.collection("DonUngTuyen")

    /**
     * Lấy tất cả đơn ứng tuyển của một Nhà Tuyển Dụng (NTD) cụ thể.
     * Đây chính là nơi ta dùng ID_NTD từ code đăng nhập của bạn.
     */
    suspend fun getDonUngTuyenCuaNTD(ntdId: String): Result<List<DonUngTuyen>> {
        return try {
            val querySnapshot = donUngTuyenCollection.whereEqualTo("ID_NTD", ntdId).get().await()

            val listDon = querySnapshot.documents.mapNotNull { doc ->
                val don = doc.toObject(DonUngTuyen::class.java)
                don?.id = doc.id
                don
            }

            // 🔥 THÊM ĐOẠN NÀY: Lấy thông tin User (Email, SĐT) cho từng đơn
            val tasks = listDon.map { don ->
                db.collection("users").document(don.idNguoiTV).get()
                    .continueWith { task ->
                        if (task.isSuccessful) {
                            val user = task.result
                            don.tenNguoiTV = user?.getString("hoTen") ?: "Ẩn danh"
                            don.emailNguoiTV = user?.getString("email") // Lấy Email
                            don.sdtNguoiTV = user?.getString("soDienThoai") // Lấy SĐT
                        }
                    }
            }
            Tasks.whenAllComplete(tasks).await() // Chờ tải xong hết mới trả về

            Result.success(listDon)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cập nhật trạng thái cho một đơn ứng tuyển
     */
    suspend fun updateTrangThaiDon(donId: String, newStatus: TrangThaiDon): Result<Unit> {
        return try {
            donUngTuyenCollection.document(donId)
                .update("trang_thai", newStatus.value)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa một đơn ứng tuyển
     */
    suspend fun deleteDonUngTuyen(donId: String): Result<Unit> {
        return try {
            donUngTuyenCollection.document(donId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}