    package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
    import com.example.app_tim_viec.R
    import com.example.app_tim_viec.UI.Nguoi_Dung.JobSearchAdapter
    import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.donungtuyen.FragmentUploadCV
    import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.UploadCVActivity
    import com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung.AnhSliderAdapter
    import com.example.app_tim_viec.databinding.FragmentChiTietViecBinding
    import com.google.firebase.firestore.FirebaseFirestore


    class FragmentChiTietViec : Fragment() {

        private var _binding: FragmentChiTietViecBinding? = null
        private val binding get() = _binding!!
        private var job: Bai_Dang_CV? = null
        private val db = FirebaseFirestore.getInstance()
        private val TAG = "ChiTietViecNTV"
        private var maBaiDang: String? = null
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentChiTietViecBinding.inflate(inflater, container, false)
            // (Tôi đã sửa lỗi từ lượt trước, bạn đã sửa các data class thành Serializable)
            job = arguments?.getSerializable("baiDang") as? Bai_Dang_CV

            job?.let {
                setupUI(it)
                loadThongTinChiTiet(it.maBaiDang ?: "")
                loadHinhAnh(it.maBaiDang ?: "")
            }

            binding.btnUngTuyen.setOnClickListener {
                job?.let { baiDang ->
                    // SỬA LẠI 2: Gọi đúng tên hàm mới
                    navigateToUploadFragment(baiDang)
                } ?: run {
                    Log.e(TAG, "Lỗi: 'job' là null, không thể ứng tuyển.")
                    Toast.makeText(context, "Lỗi, không tìm thấy thông tin việc làm", Toast.LENGTH_SHORT).show()
                }
            }

            return binding.root
        }


        // --- SỬA LẠI 3: THAY ĐỔI HOÀN TOÀN HÀM NÀY ĐỂ MỞ FRAGMENT ---
        private fun navigateToUploadFragment(baiDang: Bai_Dang_CV) {
            Log.d(TAG, "navigateToUploadFragment: Chuyển sang FragmentUploadCV với bài đăng: ${baiDang.maBaiDang}")

            // 1. Tạo Fragment đích
            val uploadFragment = FragmentUploadCV()

            // 2. Tạo Bundle và đính kèm 'baiDang' (Nguyên nhân crash NotSerializableException là ở đây)
            // (Giờ bạn đã sửa các data class con thành Serializable nên nó sẽ chạy)
            val bundle = Bundle().apply {
                putSerializable("baiDang", baiDang)
            }
            uploadFragment.arguments = bundle

            // 3. Thực hiện chuyển Fragment (thay vì dùng Intent)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, uploadFragment) // Đảm bảo R.id.fragmentContainer là ID của bạn
                .addToBackStack(null) // Cho phép người dùng nhấn 'Back' để quay lại
                .commit()
        }
        // --- KẾT THÚC THÊM MỚI ---

        private fun setupUI(job: Bai_Dang_CV) {
            Log.d(TAG, "📋 Hiển thị chi tiết bài đăng: ${job.maBaiDang}")

            binding.tvTieuDe.text = job.tieuDe
            binding.tvMoTa.text = job.moTa
            binding.tvLuong.text = "Mức lương: ${job.thongTinChiTiet?.mucLuong ?: 0} VND"
        }

        private fun loadThongTinChiTiet(maBaiDang: String) {
            db.collection("BaiDangCV")
                .document(maBaiDang)
                .collection("thongTinChiTiet")
                .get()
                .addOnSuccessListener { subDocs ->
                    if (!subDocs.isEmpty) {
                        val data = subDocs.documents.first().data

                        val kinhNghiem = data?.get("soNamKinhNghiem")?.toString() ?: ""
                        val bangCap = data?.get("bangCapToiThieu")?.toString() ?: ""
                        val gioiTinhRaw = data?.get("yeuCauGioiTinh")
                        val gioiTinhStr = when (gioiTinhRaw) {
                            is String -> gioiTinhRaw.trim()
                            is List<*> -> gioiTinhRaw.joinToString(", ") { it.toString().trim() }
                            else -> ""
                        }
                        val chucVu = data?.get("chucVu")?.toString() ?: ""
                        val diaChi = data?.get("diaChiNoiLamViec")?.toString() ?: ""
                        val ngonNgu = data?.get("ngonNguYeuCau")?.toString() ?: ""
                        val tinhThanh = data?.get("tinhThanhPho")?.toString() ?: ""
                        val soLuong = data?.get("soLuongCanTuyen")?.toString() ?: ""
                        val kyNang = data?.get("kyNangChuyenMon")?.toString() ?: ""
                        val chungChi = data?.get("chungChiYeuCau")?.toString() ?: ""

                        val min = data?.get("yeuCauDoTuoi.min") as? String ?: ""
                        val max = data?.get("yeuCauDoTuoi.max") as? String ?: ""
                        val tu = data?.get("thoiGianLamViec.tu") as? String ?: ""
                        val den = data?.get("thoiGianLamViec.den") as? String ?: ""

                        binding.tvSoNamKinhNghiem.text = "Kinh nghiệm: $kinhNghiem năm"
                        binding.tvDoTuoi.text = "Độ tuổi: $min - $max"
                        binding.tvThoiGianLamViec.text = "Thời gian: $tu - $den"
                        binding.tvBangCap.text = "Bằng cấp tối thiểu: $bangCap"
                        binding.tvGioiTinh.text = if (gioiTinhStr.isNotEmpty()) "Giới tính: $gioiTinhStr" else "Giới tính: Không xác định"
                        binding.tvChucVu.text = "Chức vụ: $chucVu"
                        binding.tvDiaChi.text = "Địa chỉ: $diaChi"
                        binding.tvNgonNgu.text = "Ngôn ngữ yêu cầu: $ngonNgu"
                        binding.tvTinhThanh.text = "Tỉnh/TP: $tinhThanh"
                        binding.tvSoLuong.text = "Số lượng cần tuyển: $soLuong"
                        binding.tvKyNang.text = "Kỹ năng: $kyNang"
                        binding.tvChungChi.text = "Chứng chỉ: $chungChi"

                        // XÓA DÒNG LỖI TẠI ĐÂY: binding.btnUngTuyen.setOnClickListener()

                        Log.d(TAG, "✅ Load thông tin chi tiết từ subcollection thành công")
                    } else {
                        Log.w(TAG, "⚠️ Không có subcollection 'thongTinChiTiet' cho $maBaiDang")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "🔥 Lỗi khi lấy thông tin chi tiết: ${e.message}")
                }
        }

        private fun loadHinhAnh(maBaiDang: String) {
            db.collection("BaiDangCV")
                .document(maBaiDang)
                .get()
                .addOnSuccessListener { doc ->
                    val dsAnh = doc.get("hinhAnh") as? List<String> ?: emptyList()
                    if (dsAnh.isNotEmpty()) {
                        val adapter = AnhSliderAdapter(dsAnh)
                        binding.viewPagerHinhAnh.adapter = adapter
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "🔥 Lỗi khi tải ảnh: ${e.message}")
                }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }