package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.FragmentTaoTinMoiBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.util.Log

class FragmentTaoTinMoi : Fragment() {

    private var _binding: FragmentTaoTinMoiBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private val selectedImages = mutableListOf<Uri>()
    private val uploadedImageUrls = mutableListOf<String>()
    private lateinit var pickImagesLauncher: ActivityResultLauncher<String>

    private var isUploading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaoTinMoiBinding.inflate(inflater, container, false)

        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                if (uris.size > 5) {
                    Toast.makeText(requireContext(), "Chỉ chọn tối đa 5 ảnh!", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                selectedImages.clear()
                uploadedImageUrls.clear()
                selectedImages.addAll(uris)
                hienThiAnhPreview()

                Log.d("TaoTinMoi", "Người dùng đã chọn ${selectedImages.size} ảnh để upload.")
                selectedImages.forEachIndexed { index, uri ->
                    Log.d("TaoTinMoi", "Ảnh ${index + 1}: $uri")
                }

                // Khi chọn ảnh -> tự upload
                uploadTatCaAnhLenCloudinary()
            }

        binding.btnChonAnh.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        binding.btnGui.setOnClickListener {
            if (isUploading) {
                Toast.makeText(requireContext(), "Đang tải ảnh lên, vui lòng chờ...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            guiBaiDang()
        }

        return binding.root
    }

    private fun hienThiAnhPreview() {
        binding.layoutAnhPreview.removeAllViews()
        val size = resources.displayMetrics.widthPixels / 3 - 20
        for (uri in selectedImages) {
            val img = ImageView(requireContext())
            img.layoutParams = ViewGroup.LayoutParams(size, size)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(uri).into(img)
            binding.layoutAnhPreview.addView(img)
        }
    }

    private fun uploadTatCaAnhLenCloudinary() {
        if (selectedImages.isEmpty()) {
            Log.w("TaoTinMoi", "Không có ảnh nào để upload.")
            return
        }

        val preset = getString(R.string.cloudinary_upload_preset)
        isUploading = true
        uploadedImageUrls.clear()

        Toast.makeText(requireContext(), "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show()
        Log.d("TaoTinMoi", "=== BẮT ĐẦU UPLOAD ẢNH LÊN CLOUDINARY ===")

        for ((index, uri) in selectedImages.withIndex()) {
            Log.d("TaoTinMoi", "Bắt đầu upload ảnh ${index + 1}: $uri")

            MediaManager.get().upload(uri)
                .unsigned(preset)
                .option("folder", "bai_dang")
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("TaoTinMoi", "Upload bắt đầu (requestId=$requestId)")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                        Log.d("TaoTinMoi", "Upload tiến độ: $progress%")
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String
                        if (!url.isNullOrEmpty()) {
                            uploadedImageUrls.add(url)
                            Log.d("TaoTinMoi", "✅ Upload thành công ảnh ${uploadedImageUrls.size}/${selectedImages.size}: $url")
                        } else {
                            Log.w("TaoTinMoi", "⚠️ Kết quả upload ảnh rỗng!")
                        }

                        // Khi tất cả ảnh upload xong
                        if (uploadedImageUrls.size == selectedImages.size) {
                            isUploading = false
                            Toast.makeText(requireContext(), "Upload ảnh hoàn tất!", Toast.LENGTH_SHORT).show()
                            Log.d("TaoTinMoi", "=== TẤT CẢ ẢNH ĐÃ UPLOAD XONG (${uploadedImageUrls.size}) ===")
                            uploadedImageUrls.forEachIndexed { i, link ->
                                Log.d("TaoTinMoi", "Ảnh ${i + 1}: $link")
                            }
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Log.e("TaoTinMoi", "❌ Lỗi upload ảnh: ${error?.description}")
                        Toast.makeText(requireContext(), "Lỗi upload: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.w("TaoTinMoi", "Upload bị hoãn lại: ${error?.description}")
                    }
                })
                .dispatch()
        }
    }

    private fun guiBaiDang() {
        val tieuDe = binding.edtTieuDe.text.toString().trim()
        val yeuCau = binding.edtYeuCau.text.toString().trim()
        val quyenLoi = binding.edtQuyenLoi.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val moTa = binding.edtMoTa.text.toString().trim()

        if (tieuDe.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (isUploading) {
            Toast.makeText(requireContext(), "Ảnh đang được tải lên, vui lòng chờ!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImages.isNotEmpty() && uploadedImageUrls.size != selectedImages.size) {
            Toast.makeText(requireContext(), "Chưa upload xong tất cả ảnh!", Toast.LENGTH_SHORT).show()
            Log.w("TaoTinMoi", "⚠️ Chưa upload đủ ảnh (${uploadedImageUrls.size}/${selectedImages.size})")
            return
        }

        val maBaiDang = "BD" + System.currentTimeMillis().toString().takeLast(4)
        val maThongTin = "TTCB_" + System.currentTimeMillis().toString().takeLast(3)

        Log.d("TaoTinMoi", "=== BẮT ĐẦU GỬI BÀI ===")
        Log.d("TaoTinMoi", "Tiêu đề: $tieuDe")
        Log.d("TaoTinMoi", "Email: $email")
        Log.d("TaoTinMoi", "Danh sách URL ảnh (${uploadedImageUrls.size}): ${uploadedImageUrls.joinToString()}")

        val baiDangCV = hashMapOf(
            "maNhaTuyenDung" to "QmmIgmOVvnXVRkXlVHz3KRng0y22",
            "maBaiDang" to maBaiDang,
            "thoiGianTao" to Timestamp.now(),
            "thoiGianHetHan" to Timestamp(Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)),
            "hinhAnh" to uploadedImageUrls
        )

        val thongTinCoBan = hashMapOf(
            "Tieu_De" to tieuDe,
            "Yeu_Cau_CV" to yeuCau,
            "Quyen_Loi" to quyenLoi,
            "Email_Lien_He" to email,
            "Mo_Ta" to moTa
        )

        val baiDangRef = db.collection("BaiDangCV").document(maBaiDang)
        baiDangRef.set(baiDangCV)
            .addOnSuccessListener {
                Log.d("TaoTinMoi", "✅ Lưu document chính thành công (maBaiDang=$maBaiDang)")
                baiDangRef.collection("thongTinCoBan").document(maThongTin).set(thongTinCoBan)
                    .addOnSuccessListener {
                        Log.d("TaoTinMoi", "✅ Lưu subcollection 'thongTinCoBan' thành công")
                        Toast.makeText(requireContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, FragmentQLTinTuyenDung())
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaoTinMoi", "❌ Lỗi ghi subcollection: ${e.message}")
                        Toast.makeText(requireContext(), "Lỗi ghi sub: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("TaoTinMoi", "❌ Lỗi ghi Firestore chính: ${e.message}")
                Toast.makeText(requireContext(), "Lỗi ghi chính: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
