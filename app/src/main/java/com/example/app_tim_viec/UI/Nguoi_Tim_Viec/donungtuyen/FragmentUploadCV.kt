package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.donungtuyen

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.DonUngTuyen
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.DonUngTuyen.TrangThaiDon
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.FragmentUploadCvBinding
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.SupabaseModule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class FragmentUploadCV : Fragment() {

    private val TAG = "FragmentUploadCV"

    private var _binding: FragmentUploadCvBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val supabaseStorage by lazy { SupabaseModule.client.storage["CVS"] }

    private var job: Bai_Dang_CV? = null
    private var fileUri: Uri? = null
    private var originalFileName: String = "file_cv.pdf"

    // --- SỬA LẠI 1: DÙNG OpenDocument GIỐNG NHƯ ACTIVITY ĐANG CHẠY TỐT ---
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument() // Đổi từ GetContent sang OpenDocument
    ) { uri: Uri? ->
        if (uri != null) {
            fileUri = uri
            originalFileName = getFileName(uri) ?: "file_cv.pdf" // Dùng hàm getFileName mới
            Log.d(TAG, "filePickerLauncher: Đã chọn file: $originalFileName (URI: $uri)")

            if (_binding != null) {
                binding.tvTenFile.text = originalFileName
                binding.btnNopDon.isEnabled = true
            } else {
                Log.w(TAG, "filePickerLauncher: Binding đã null, không cập nhật UI")
            }
        } else {
            Log.w(TAG, "filePickerLauncher: Người dùng không chọn file")
        }
    }
    // --- KẾT THÚC SỬA LẠI 1 ---

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Bắt đầu")
        _binding = FragmentUploadCvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Bắt đầu")

        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            Log.d(TAG, "onViewCreated: Khởi tạo Firebase Auth & Firestore OK")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: CRASH khi khởi tạo Firebase: ${e.message}", e)
        }

        try {
            job = arguments?.getSerializable("baiDang") as? Bai_Dang_CV
            if (job == null) {
                Log.e(TAG, "onViewCreated: Lỗi NGHIÊM TRỌNG: 'baiDang' nhận được là NULL")
                Toast.makeText(requireContext(), "Lỗi: Không có thông tin việc làm", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
                return
            }
            Log.d(TAG, "onViewCreated: Nhận 'baiDang' OK: ${job!!.maBaiDang}")
            Log.d(TAG, "onViewCreated: Tiêu đề: ${job!!.tieuDe}")
            Log.d(TAG, "onViewCreated: Mã NTD: ${job!!.maNhaTuyenDung}")

        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: CRASH khi lấy 'baiDang' từ arguments: ${e.message}", e)
            parentFragmentManager.popBackStack()
            return
        }

        binding.tvTenViec.text = "Ứng tuyển: ${job?.tieuDe}"

        // --- SỬA LẠI 2: CẬP NHẬT LAUNCHER ĐỂ KHỚP VỚI OpenDocument ---
        binding.btnChonCV.setOnClickListener {
            Log.d(TAG, "btnChonCV: Đã click, mở trình chọn file...")
            try {
                // Chỉ cho phép chọn PDF và Word (giống Activity đang chạy tốt)
                filePickerLauncher.launch(
                    arrayOf("application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                )
            } catch (e: Exception) {
                Log.e(TAG, "btnChonCV: CRASH khi gọi filePickerLauncher: ${e.message}", e)
            }
        }
        // --- KẾT THÚC SỬA LẠI 2 ---

        binding.btnNopDon.setOnClickListener {
            Log.d(TAG, "btnNopDon: Đã click, bắt đầu uploadAndApply...")
            uploadAndApply()
        }
        Log.d(TAG, "onViewCreated: Hoàn tất")
    }

    private fun uploadAndApply() {
        Log.d(TAG, "uploadAndApply: Bắt đầu quá trình")

        val uri = fileUri
        if (uri == null) {
            Log.w(TAG, "uploadAndApply: Lỗi - fileUri là null. Người dùng chưa chọn file.")
            Toast.makeText(context, "Vui lòng chọn file CV", Toast.LENGTH_SHORT).show()
            return
        }

        val currentJob = job
        if (currentJob == null) {
            Log.e(TAG, "uploadAndApply: Lỗi - currentJob là null. Không thể tiếp tục.")
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e(TAG, "uploadAndApply: Lỗi - uid là null, người dùng chưa đăng nhập?")
            return
        }
        Log.d(TAG, "uploadAndApply: Đang ứng tuyển: uid='$uid', job='${currentJob.maBaiDang}'")

        val idNhaTuyenDung = currentJob.maNhaTuyenDung
        if (idNhaTuyenDung.isNullOrBlank()) {
            Log.e(TAG, "uploadAndApply: Lỗi NGHIÊM TRỌNG - 'maNhaTuyenDung' trong job bị rỗng!")
            Toast.makeText(requireContext(), "Lỗi: Bài đăng thiếu ID nhà tuyển dụng", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "uploadAndApply: Thông tin hợp lệ. Bắt đầu setLoading(true)")
        setLoading(true)

        // --- DÙNG HÀM getBytesFromUri MỚI (AN TOÀN) ---
        val fileBytes = getBytesFromUri(uri)
        if (fileBytes == null) {
            Log.e(TAG, "uploadAndApply: Lỗi - getBytesFromUri trả về null. Không thể đọc file.")
            Toast.makeText(requireContext(), "Không thể đọc file đã chọn", Toast.LENGTH_SHORT).show()
            setLoading(false)
            return
        }
        Log.d(TAG, "uploadAndApply: Đọc file thành công (${fileBytes.size} bytes)")

        val fileExtension = getFileExtension(originalFileName) // Dùng hàm getFileExtension mới
        val uniqueFileName = "cv_${UUID.randomUUID()}.$fileExtension"
        val storagePath = "$uid/$uniqueFileName"
        Log.d(TAG, "uploadAndApply: Tên file trên storage: $storagePath")

        lifecycleScope.launch {
            try {
                Log.d(TAG, "uploadAndApply (Coroutine): Bắt đầu upload lên Supabase...")
                supabaseStorage.upload(
                    path = storagePath,
                    data = fileBytes,
                    upsert = false
                )
                Log.d(TAG, "uploadAndApply (Coroutine): Upload Supabase THÀNH CÔNG")

                val publicUrl = supabaseStorage.publicUrl(storagePath)
                Log.d(TAG, "uploadAndApply (Coroutine): Lấy public URL thành công: $publicUrl")

                val donUngTuyen = DonUngTuyen(
                    id = "",
                    trangThai = TrangThaiDon.CHUA_XU_LY.value,
                    ngayNop = Timestamp.now(),
                    idBaiDang = currentJob.maBaiDang?:"",
                    idNTD = idNhaTuyenDung,
                    idNguoiTV = uid,
                    urlFileCV = publicUrl ,
                    tieuDe = currentJob.tieuDe?:""
                )
                Log.d(TAG, "uploadAndApply (Coroutine): Tạo object DonUngTuyen OK")

                Log.d(TAG, "uploadAndApply (Coroutine): Bắt đầu lưu vào Firestore...")
                db.collection("DonUngTuyen")
                    .add(donUngTuyen)
                    .addOnSuccessListener {
                        if (!isAdded || _binding == null) {
                            Log.w(TAG, "Firestore Success: Fragment đã bị hủy, không làm gì cả")
                            return@addOnSuccessListener
                        }
                        Log.d(TAG, "Firestore Success: Nộp đơn thành công! Document ID: ${it.id}")
                        Toast.makeText(requireContext(), "Nộp đơn thành công!", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        if (!isAdded || _binding == null) {
                            Log.w(TAG, "Firestore Failure: Fragment đã bị hủy, không làm gì cả")
                            return@addOnFailureListener
                        }
                        Log.e(TAG, "Firestore Failure: Lỗi khi lưu đơn (DB): ${e.message}", e)
                        Toast.makeText(requireContext(), "Lỗi khi lưu đơn (DB)", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    }

            } catch (e: Exception) {
                val safeContext = context
                if (!isAdded || _binding == null || safeContext == null) {
                    Log.e(TAG, "CRASH (Coroutine): ${e.message} (NHƯNG fragment đã bị hủy)", e)
                    return@launch
                }
                Log.e(TAG, "CRASH (Coroutine): Lỗi nghiêm trọng (thường là Supabase hoặc mạng): ${e.message}", e)
                Toast.makeText(safeContext, "Lỗi Upload (Storage): ${e.message}", Toast.LENGTH_LONG).show()
                setLoading(false)
            }
        }
    }

    // --- SỬA LẠI 3: TOÀN BỘ CÁC HÀM TIỆN ÍCH ĐƯỢC COPY TỪ ACTIVITY ĐANG CHẠY TỐT ---

    private fun setLoading(isLoading: Boolean) {
        if (!isAdded || _binding == null) {
            Log.w(TAG, "setLoading: Fragment bị hủy, không thể set loading = $isLoading")
            return
        }
        Log.d(TAG, "setLoading: $isLoading")
        binding.progressBarUpload.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnNopDon.isEnabled = !isLoading
        binding.btnChonCV.isEnabled = !isLoading
    }

    // Hàm đọc file Uri sang ByteArray (Lấy từ Activity chạy tốt)
    private fun getBytesFromUri(uri: Uri): ByteArray? {
        Log.d(TAG, "getBytesFromUri: Bắt đầu đọc bytes từ URI: $uri")
        return try {
            val inputStream = requireActivity().contentResolver.openInputStream(uri) // Dùng requireActivity()
            val byteBuffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var len: Int
            while (inputStream!!.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            val bytes = byteBuffer.toByteArray()
            Log.d(TAG, "getBytesFromUri: Đọc thành công ${bytes.size} bytes")
            bytes
        } catch (e: Exception) {
            Log.e(TAG, "getBytesFromUri: Lỗi khi đọc file thành bytes: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    // Hàm lấy tên file gốc từ Uri (Lấy từ Activity chạy tốt)
    private fun getFileName(uri: Uri): String {
        var fileName = "unknown_file"
        Log.d(TAG, "getFileName: Lấy tên file từ URI: $uri")
        try {
            requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFileName: Lỗi khi lấy tên file: ${e.message}", e)
        }
        Log.d(TAG, "getFileName: Tên file là: $fileName")
        return fileName
    }

    // Hàm lấy đuôi file (Lấy từ Activity chạy tốt)
    private fun getFileExtension(fileName: String): String {
        // Trả về chuỗi rỗng nếu không có đuôi, an toàn hơn
        val extension = fileName.substringAfterLast('.', "")
        Log.d(TAG, "getFileExtension: Tên file '$fileName' có đuôi là '$extension'")
        return extension
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Binding đã được dọn dẹp")
        _binding = null
    }
}