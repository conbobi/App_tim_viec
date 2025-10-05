package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.databinding.FragmentTtChiTietCvBinding
import com.example.app_tim_viec.R
class Fragment_TT_Chi_Tiet_CV : Fragment() {
    private var _binding: FragmentTtChiTietCvBinding? = null
    private val binding get() = _binding!!
    private var baiDang: Bai_Dang_CV? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baiDang = arguments?.getSerializable("baiDang") as? Bai_Dang_CV
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTtChiTietCvBinding.inflate(inflater, container, false)

        baiDang?.let { job ->
            binding.tvTitle.text = job.tieuDe
            binding.tvMoTa.text = job.moTa
            binding.tvLuong.text = "Mức lương: ${job.thongTinChiTiet?.mucLuong ?: 0} VND"

            // Thêm các field chi tiết
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
        }
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
