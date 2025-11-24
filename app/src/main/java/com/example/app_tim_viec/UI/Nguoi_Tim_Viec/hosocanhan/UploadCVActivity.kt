package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log // <-- THÊM LOG
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.SupabaseModule
import com.example.app_tim_viec.databinding.ActivityUploadCvBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

// --- THÊM CÁC IMPORT CẦN THIẾT ---
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUngTuyen
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.TrangThaiDon
import com.google.firebase.Timestamp
// --- KẾT THÚC THÊM IMPORT ---

import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

class UploadCVActivity : AppCompatActivity() {

    private val TAG = "UploadCVActivity" // <-- THÊM TAG

    private lateinit var binding: ActivityUploadCvBinding
    private var fileUri: Uri? = null
    private var originalFileName: String = ""

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val supabaseStorage = SupabaseModule.client.storage

    // --- BIẾN MỚI ĐỂ NHẬN DỮ LIỆU TỪ INTENT ---
    private var jobId: String? = null
    private var ntdId: String? = null
    private var jobTitle: String? = null
    private var isApplyMode: Boolean = false // Biến cờ để xác định chế độ

    // Bộ chọn file
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            fileUri = uri
            originalFileName = getFileNameFromUri(uri)
            binding.tvFileName.text = originalFileName
            binding.btnUpload.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadCvBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Bắt đầu")

        // --- NHẬN DỮ LIỆU TỪ INTENT ---
        jobId = intent.getStringExtra("MA_BAI_DANG")
        ntdId = intent.getStringExtra("MA_NHA_TUYEN_DUNG")
        jobTitle = intent.getStringExtra("TIEU_DE_BAI_DANG")

        // Kiểm tra xem đây có phải là chế độ "Nộp đơn" không
        isApplyMode = !jobId.isNullOrBlank() && !ntdId.isNullOrBlank()

        if (isApplyMode) {
            Log.d(TAG, "onCreate: Đang ở [CHẾ ĐỘ NỘP ĐƠN] cho việc: $jobTitle")
            // Cập nhật UI cho rõ ràng (bạn cần thêm tvTitle vào XML nếu muốn)
            // binding.tvTitle.text = "Nộp CV cho: $jobTitle"
            binding.btnUpload.text = "Nộp đơn" // Đổi tên nút
        } else {
            Log.d(TAG, "onCreate: Đang ở [CHẾ ĐỘ UPLOAD CV CHUNG]")
            // binding.tvTitle.text = "Upload CV Mới"
            binding.btnUpload.text = "Upload"
        }
        // --- KẾT THÚC NHẬN DỮ LIỆU ---

        binding.btnChooseFile.setOnClickListener {
            pickFile()
        }

        binding.btnUpload.setOnClickListener {
            uploadFile()
        }
    }

    private fun pickFile() {
        filePickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
    }

    private fun uploadFile() {
        val uri = fileUri ?: return
        val uid = auth.currentUser?.uid ?: return

        setLoading(true)

        val fileBytes = getBytesFromUri(uri)
        if (fileBytes == null) {
            Toast.makeText(this, "Không thể đọc file", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }

        val fileExtension = getFileExtension(originalFileName)
        val uniqueFileName = "cv_${UUID.randomUUID()}.$fileExtension"
        val storagePath = "$uid/$uniqueFileName"
        Log.d(TAG, "uploadFile: Bắt đầu upload $storagePath")

        lifecycleScope.launch {
            try {
                // Bước 1: Upload lên Supabase Storage (Giữ nguyên)
                supabaseStorage.from("CVS").upload(
                    path = storagePath,
                    data = fileBytes,
                    upsert = false
                )
                Log.d(TAG, "uploadFile: Upload Supabase thành công")

                // Bước 2: Lấy public URL (Giữ nguyên)
                val publicUrl = supabaseStorage.from("CVS").publicUrl(storagePath)
                Log.d(TAG, "uploadFile: Lấy URL thành công: $publicUrl")

                // --- SỬA LẠI BƯỚC 3: LƯU VÀO FIRESTORE (PHÂN NHÁNH LOGIC) ---
                if (isApplyMode) {
                    // CHẾ ĐỘ 1: NỘP ĐƠN (Tạo DonUngTuyen)
                    Log.d(TAG, "uploadFile: Lưu vào DonUngTuyen...")

                    val donUngTuyen = DonUngTuyen(
                        id = "", // Firestore sẽ tự tạo
                        trangThai = TrangThaiDon.CHUA_XU_LY.value,
                        ngayNop = Timestamp.now(), // Cần import Timestamp
                        idBaiDang = jobId!!, // Lấy từ intent
                        idNTD = ntdId!!, // Lấy từ intent
                        idNguoiTV = uid, // ID người đang nộp
                        urlFileCV = publicUrl, // Link Supabase
                        tieuDe = jobTitle!! // Lấy từ intent
                    )

                    db.collection("DonUngTuyen")
                        .add(donUngTuyen)
                        .addOnSuccessListener {
                            Log.d(TAG, "uploadFile: Nộp đơn thành công, doc ID: ${it.id}")
                            Toast.makeText(this@UploadCVActivity, "Nộp đơn thành công!", Toast.LENGTH_SHORT).show()
                            setLoading(false)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "uploadFile: Lỗi khi lưu DonUngTuyen", e)
                            Toast.makeText(this@UploadCVActivity, "Lỗi khi lưu đơn (DB): ${e.message}", Toast.LENGTH_SHORT).show()
                            setLoading(false)
                        }

                } else {
                    // CHẾ ĐỘ 2: UPLOAD CV CHUNG (Logic cũ)
                    Log.d(TAG, "uploadFile: Lưu vào collection CVS...")
                    val cvData = hashMapOf(
                        "userId" to uid,
                        "fileName" to originalFileName,
                        "storagePath" to storagePath,
                        "downloadUrl" to publicUrl,
                        "uploadedAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("CVS")
                        .add(cvData)
                        .addOnSuccessListener {
                            Toast.makeText(this@UploadCVActivity, "Upload CV thành công!", Toast.LENGTH_SHORT).show()
                            setLoading(false)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "uploadFile: Lỗi khi lưu CVS", e)
                            Toast.makeText(this@UploadCVActivity, "Lỗi khi lưu (DB)", Toast.LENGTH_SHORT).show()
                            setLoading(false)
                        }
                }
                // --- KẾT THÚC SỬA BƯỚC 3 ---

            } catch (e: Exception) {
                Log.e(TAG, "uploadFile: Lỗi CRASH khi upload (Supabase/Mạng)", e)
                Toast.makeText(this@UploadCVActivity, "Lỗi Upload (Storage): ${e.message}", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
        }
    }

    // --- Các hàm tiện ích (Giữ nguyên) ---

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnUpload.isEnabled = !isLoading
        binding.btnChooseFile.isEnabled = !isLoading
    }

    private fun getBytesFromUri(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len: Int
            while (inputStream!!.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            byteBuffer.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown_file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
}