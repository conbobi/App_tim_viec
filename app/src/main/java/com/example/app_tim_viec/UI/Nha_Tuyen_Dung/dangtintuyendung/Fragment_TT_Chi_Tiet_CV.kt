package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.databinding.FragmentTtChiTietCvBinding
import com.example.app_tim_viec.R
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_TT_Chi_Tiet_CV : Fragment() {
    private var _binding: FragmentTtChiTietCvBinding? = null
    private val binding get() = _binding!!
    private var baiDang: Bai_Dang_CV? = null
    private val TAG = "ChiTietCV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baiDang = arguments?.getSerializable("baiDang") as? Bai_Dang_CV
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTtChiTietCvBinding.inflate(inflater, container, false)
        val db = FirebaseFirestore.getInstance()

        baiDang?.let { job ->
            Log.d(TAG, "📋 Hiển thị chi tiết bài đăng: ${job.maBaiDang}")

            // Thông tin cơ bản
            binding.tvTitle.text = job.tieuDe
            binding.tvMoTa.text = job.moTa
            binding.tvLuong.text = "Mức lương: ${job.thongTinChiTiet?.mucLuong ?: 0} VND"

            // Thông tin chi tiết
            binding.tvSoNamKinhNghiem.text = "Kinh nghiệm: ${job.thongTinChiTiet?.soNamKinhNghiem ?: 0} năm"
            binding.tvDoTuoi.text = "Độ tuổi: ${job.thongTinChiTiet?.yeuCauDoTuoi?.min} - ${job.thongTinChiTiet?.yeuCauDoTuoi?.max}"
            binding.tvThoiGianLamViec.text = "Thời gian: ${job.thongTinChiTiet?.thoiGianLamViec?.tu} - ${job.thongTinChiTiet?.thoiGianLamViec?.den}"
            binding.tvBangCap.text = "Bằng cấp tối thiểu: ${job.thongTinChiTiet?.bangCapToiThieu ?: "Không yêu cầu"}"
            binding.tvGioiTinh.text = "Giới tính: ${job.thongTinChiTiet?.yeuCauGioiTinh ?: "Không yêu cầu"}"
            binding.tvSoDienThoai.text = "SĐT: ${job.thongTinChiTiet?.soDienThoaiTuyenDung ?: ""}"
            binding.tvChucVu.text = "Chức vụ: ${job.thongTinChiTiet?.chucVu ?: ""}"
            binding.tvKyNang.text = "Kỹ năng: ${job.thongTinChiTiet?.kyNangChuyenMon?.joinToString(", ")}"
            binding.tvDiaChi.text = "Địa chỉ: ${job.thongTinChiTiet?.diaChiNoiLamViec ?: ""}"
            binding.tvNgonNgu.text = "Ngôn ngữ yêu cầu: ${job.thongTinChiTiet?.ngonNguYeuCau ?: ""}"
            binding.tvTinhThanh.text = "Tỉnh/TP: ${job.thongTinChiTiet?.tinhThanhPho ?: ""}"
            binding.tvSoLuong.text = "Số lượng cần tuyển: ${job.thongTinChiTiet?.soLuongCanTuyen ?: 0}"
            binding.tvChungChi.text = "Chứng chỉ: ${job.thongTinChiTiet?.chungChiYeuCau?.joinToString(", ")}"

            Log.d(TAG, "🔍 Tải danh sách ảnh từ collection 'BaiDangCV'...")

            // 🔹 Lấy ảnh trực tiếp từ collection BaiDangCV
            db.collection("BaiDangCV")
                .document(job.maBaiDang?:"")
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val dsAnh = doc.get("hinhAnh") as? List<String> ?: emptyList()
                        Log.d(TAG, "✅ Lấy được ${dsAnh.size} ảnh từ 'BaiDangCV'")

                        if (dsAnh.isNotEmpty()) {
                            val adapter = AnhSliderAdapter(dsAnh)
                            binding.viewPagerHinhAnh.adapter = adapter
                        } else {
                            Log.w(TAG, "⚠️ Document 'BaiDangCV' có nhưng không có ảnh!")
                        }
                    } else {
                        Log.e(TAG, "❌ Không tìm thấy document ${job.maBaiDang} trong 'BaiDangCV'")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "🔥 Lỗi khi truy vấn 'BaiDangCV': ${e.message}")
                }
        }


        // load dữ liệu các trường thiếu
        // ✅ Đúng: dùng trực tiếp baiDang?.maBaiDang
        baiDang?.maBaiDang?.let { maBaiDang ->
            db.collection("BaiDangCV")
                .document(maBaiDang)
                .collection("thongTinChiTiet")
                .get()
                .addOnSuccessListener { subDocs  ->
                    if (!subDocs.isEmpty) {
                        val data = subDocs.documents.first().data



                        val min = data?.get("yeuCauDoTuoi.min") as? String ?: ""
                        val max = data?.get("yeuCauDoTuoi.max") as? String ?: ""


                        val tu = data?.get("thoiGianLamViec.tu") as? String ?: ""
                        val den = data?.get("thoiGianLamViec.den") as? String ?: ""
                        val kyNang = data?.get("kyNangChuyenMon") as? String ?: ""
                        val chungChi = data?.get("chungChiYeuCau") as? String ?: ""

                        binding.tvDoTuoi.text = "Độ tuổi: $min - $max"
                        binding.tvThoiGianLamViec.text = "Thời gian: $tu - $den"
                        binding.tvKyNang.text = "Kỹ năng: $kyNang"
                        binding.tvChungChi.text = "Chứng chỉ: $chungChi"
                    }
                }
        }




        // Nút chỉnh sửa bài đăng
        binding.btnEdit.setOnClickListener {
            val fragment = FragmentSuaTin()
            val bundle = Bundle().apply {
                putSerializable("baiDang", baiDang)
            }
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}