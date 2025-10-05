package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Bai_Dang_CV
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.DoTuoi
import com.example.app_tim_viec.R
import com.example.app_tim_viec.databinding.FragmentSuaTinBinding
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory.BaiDangRepository
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.thoiGianLamViec
import kotlinx.coroutines.launch

class FragmentSuaTin : Fragment() {
    private var _binding: FragmentSuaTinBinding? = null
    private val binding get() = _binding!!

    private var baiDang: Bai_Dang_CV? = null
    private val repository = BaiDangRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baiDang = arguments?.getSerializable("baiDang") as? Bai_Dang_CV
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuaTinBinding.inflate(inflater, container, false)

        // 🟢 Fill data như cũ
        baiDang?.let { job ->
            binding.edtTitle.setText(job.tieuDe)
            binding.edtMoTa.setText(job.moTa)
            binding.edtYeuCauCV.setText(job.yeuCauCV)
            binding.edtQuyenLoi.setText(job.quyenLoi)
            binding.edtEmailLienHe.setText(job.emailLienHe)
            // ... các field khác
        }

        binding.btnSave.setOnClickListener {
            baiDang?.let { job ->
                // Cập nhật local object
                job.apply {
                    tieuDe = binding.edtTitle.text.toString()
                    moTa = binding.edtMoTa.text.toString()
                    yeuCauCV = binding.edtYeuCauCV.text.toString()
                    quyenLoi = binding.edtQuyenLoi.text.toString()
                    emailLienHe = binding.edtEmailLienHe.text.toString()
                    thongTinChiTiet?.apply {
                        mucLuong = binding.edtLuong.text.toString().toIntOrNull() ?: 0
                        soNamKinhNghiem = binding.edtSoNamKN.text.toString().toIntOrNull() ?: 0
                        yeuCauDoTuoi = DoTuoi(
                            min = binding.edtDoTuoiMin.text.toString().toIntOrNull() ?: 0,
                            max = binding.edtDoTuoiMax.text.toString().toIntOrNull() ?: 0
                        )
                        thoiGianLamViec = thoiGianLamViec(
                            tu = binding.edtThoiGianTu.text.toString(),
                            den = binding.edtThoiGianDen.text.toString()
                        )
                        bangCapToiThieu = binding.edtBangCap.text.toString()
                        yeuCauGioiTinh = binding.edtGioiTinh.text.toString()
                        soDienThoaiTuyenDung = binding.edtSoDienThoai.text.toString()
                        chucVu = binding.edtChucVu.text.toString()
                        kyNangChuyenMon = binding.edtKyNang.text.toString().split(",").map { it.trim() }
                        diaChiNoiLamViec = binding.edtDiaChi.text.toString()
                        ngonNguYeuCau = binding.edtNgonNgu.text.toString()
                        tinhThanhPho = binding.edtTinhThanh.text.toString()
                        soLuongCanTuyen = binding.edtSoLuong.text.toString().toIntOrNull() ?: 0
                        chungChiYeuCau = binding.edtChungChi.text.toString().split(",").map { it.trim() }
                    }
                }

                // 🟢 Gọi Repository update (chạy trong coroutine)
                lifecycleScope.launch {
                    try {
                        repository.updateBaiDang(job)
                        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }
}

