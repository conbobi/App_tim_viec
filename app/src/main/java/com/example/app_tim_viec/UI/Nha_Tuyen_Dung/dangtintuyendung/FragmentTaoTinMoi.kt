package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung


import com.cloudinary.android.callback.ErrorInfo
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.callback.UploadCallback
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.FragmentTaoTinMoiBinding
import com.cloudinary.android.MediaManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth

class FragmentTaoTinMoi : Fragment() {

    private var _binding: FragmentTaoTinMoiBinding? = null

    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val selectedImages = mutableListOf<Uri>()
    private val uploadedImageUrls = mutableListOf<String>()
    private var isUploading = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaoTinMoiBinding.inflate(inflater, container, false)

        // ⚡ Gắn sự kiện chọn ngày và giờ
        setupPickers()


        binding.btnChonAnh.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }
        binding.btnGui.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
                uploadTatCaAnhLenCloudinary() // Chỉ gọi upload ảnh, không gọi guiBaiDang ở đây
            } else {
                guiBaiDang() // Nếu không có ảnh thì gửi trực tiếp
            }
        }

        return binding.root
    }

    private fun setupPickers() {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 🔹 Chọn ngày cho độ tuổi min


        // 🔹 Chọn ngày cho độ tuổi max


        // 🔹 Chọn giờ làm việc từ
        binding.edtGioTu.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    binding.edtGioTu.setText(timeFormat.format(cal.time))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }

        // 🔹 Chọn giờ làm việc đến
        binding.edtGioDen.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    binding.edtGioDen.setText(timeFormat.format(cal.time))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    private fun guiBaiDang() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val maNhaTuyenDung = currentUser?.uid ?: return
        val tieuDe = binding.edtTieuDe.text.toString()
        val yeuCau = binding.edtYeuCau.text.toString()
        val quyenLoi = binding.edtQuyenLoi.text.toString()
        val email = binding.edtEmail.text.toString()
        val moTa = binding.edtMoTa.text.toString()

        val soNamKinhNghiem = binding.edtSoNamKinhNghiem.text.toString().toIntOrNull() ?: 0
        val bangCap = binding.edtBangCapToiThieu.text.toString()
        val gioiTinh = binding.edtYeuCauGioiTinh.text.toString()
        val soLuong = binding.edtSoLuongCanTuyen.text.toString().toIntOrNull() ?: 0
        val mucLuong = binding.edtMucLuong.text.toString().toIntOrNull() ?: 0
        val chucVu = binding.edtChucVu.text.toString()
        val kyNang = binding.edtKyNangChuyenMon.text.toString()
        val chungChi = binding.edtChungChiYeuCau.text.toString()
        val ngonNgu = binding.edtNgonNguYeuCau.text.toString()
        val diaChi = binding.edtDiaChiNoiLamViec.text.toString()
        val tinhThanh = binding.edtTinhThanhPho.text.toString()
        val tuoiMin = binding.edtTuoiMin.text.toString()
        val tuoiMax = binding.edtTuoiMax.text.toString()
        val gioTu = binding.edtGioTu.text.toString()
        val gioDen = binding.edtGioDen.text.toString()

        if (tieuDe.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        val maBaiDang = "BD" + System.currentTimeMillis().toString().takeLast(4)
        val maThongTinCB = "TTCB_" + System.currentTimeMillis().toString().takeLast(3)
        val maThongTinCT = "TTCT_" + System.currentTimeMillis().toString().takeLast(3)

        val baiDangCV = hashMapOf(
            "maNhaTuyenDung" to maNhaTuyenDung,
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

        val thongTinChiTiet = hashMapOf(
            "soNamKinhNghiem" to soNamKinhNghiem,
            "bangCapToiThieu" to bangCap,
            "yeuCauGioiTinh" to gioiTinh,
            "soLuongCanTuyen" to soLuong,
            "mucLuong" to mucLuong,
            "yeuCauDoTuoi.min" to tuoiMin,
            "yeuCauDoTuoi.max" to tuoiMax,
            "thoiGianLamViec.tu" to gioTu,
            "thoiGianLamViec.den" to gioDen,
            "chucVu" to chucVu,
            "kyNangChuyenMon" to kyNang,
            "chungChiYeuCau" to chungChi,
            "ngonNguYeuCau" to ngonNgu,
            "diaChiNoiLamViec" to diaChi,
            "tinhThanhPho" to tinhThanh
        )

        val baiDangRef = db.collection("BaiDangCV").document(maBaiDang)

        baiDangRef.set(baiDangCV)
            .addOnSuccessListener {
                baiDangRef.collection("thongTinCoBan").document(maThongTinCB).set(thongTinCoBan)
                    .addOnSuccessListener {
                        baiDangRef.collection("thongTinChiTiet").document(maThongTinCT).set(thongTinChiTiet)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragmentContainer, FragmentQLTinTuyenDung())
                                    .addToBackStack(null)
                                    .commit()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Lỗi ghi chi tiết: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Lỗi ghi cơ bản: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi ghi bài đăng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
        if (selectedImages.isEmpty()) return
        val preset = getString(R.string.cloudinary_upload_preset)
        isUploading = true
        uploadedImageUrls.clear()

        Toast.makeText(requireContext(), "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show()

        for ((index, uri) in selectedImages.withIndex()) {
            MediaManager.get().upload(uri)
                .unsigned(preset)
                .option("folder", "bai_dang")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = resultData?.get("secure_url") as? String
                        if (!url.isNullOrEmpty()) {
                            uploadedImageUrls.add(url)
                        }

                        // Khi tất cả ảnh đã upload xong
                        if (uploadedImageUrls.size == selectedImages.size) {
                            isUploading = false
                            Toast.makeText(requireContext(), "Upload ảnh thành công!", Toast.LENGTH_SHORT).show()
                            guiBaiDang() // ✅ chỉ gửi bài sau khi ảnh xong
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        isUploading = false
                        Toast.makeText(requireContext(), "Lỗi upload: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
    }

    // Khai báo launcher chọn nhiều ảnh
    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris : List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages.clear()
            uploadedImageUrls.clear()
            selectedImages.addAll(uris.take(5)) // giới hạn 5 ảnh
            hienThiAnhPreview()
            uploadTatCaAnhLenCloudinary()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
