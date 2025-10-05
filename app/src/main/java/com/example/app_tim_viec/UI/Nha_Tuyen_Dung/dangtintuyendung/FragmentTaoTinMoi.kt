package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentTaoTinMoiBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.example.app_tim_viec.R
class FragmentTaoTinMoi : Fragment() {

    private var _binding: FragmentTaoTinMoiBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaoTinMoiBinding.inflate(inflater, container, false)

        binding.btnGui.setOnClickListener {
            guiBaiDang()
        }

        return binding.root
    }

    private fun guiBaiDang() {
        val tieuDe = binding.edtTieuDe.text.toString()
        val yeuCau = binding.edtYeuCau.text.toString()
        val quyenLoi = binding.edtQuyenLoi.text.toString()
        val email = binding.edtEmail.text.toString()
        val moTa = binding.edtMoTa.text.toString()

        if (tieuDe.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        // Sinh mã bài đăng ngẫu nhiên (VD: BDxxx)
        val maBaiDang = "BD" + System.currentTimeMillis().toString().takeLast(4)
        val maThongTin = "TTCB_" + System.currentTimeMillis().toString().takeLast(3)

        val baiDangCV = hashMapOf(
            "maNhaTuyenDung" to "QmmIgmOVvnXVRkXlVHz3KRng0y22", // TODO: lấy từ user login
            "maBaiDang" to maBaiDang,
            "thoiGianTao" to Timestamp.now(),
            "thoiGianHetHan" to Timestamp(Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // +30 ngày
        )

        val thongTinCoBan = hashMapOf(
            "Tieu_De" to tieuDe,
            "Yeu_Cau_CV" to yeuCau,
            "Quyen_Loi" to quyenLoi,
            "Email_Lien_He" to email,
            "Mo_Ta" to moTa
        )

        // Lưu lên Firestore
        val baiDangRef = db.collection("BaiDangCV").document(maBaiDang)
        baiDangRef.set(baiDangCV)
            .addOnSuccessListener {
                baiDangRef.collection("thongTinCoBan").document(maThongTin).set(thongTinCoBan)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                        // Quay về FragmentQLTinTuyenDung
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, FragmentQLTinTuyenDung()) // fragmentContainer là id FrameLayout trong activity
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Lỗi ghi sub: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi ghi chính: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
