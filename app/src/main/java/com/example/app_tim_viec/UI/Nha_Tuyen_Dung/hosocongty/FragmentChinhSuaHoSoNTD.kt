package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.hosocongty

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_tim_viec.R
import com.example.app_tim_viec.Data.Nha_Tuyen_Dung.Bai_Dang.Reponsitory.HoSoNTD_Reponsition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentChinhSuaHoSoNTD : Fragment() {

    private lateinit var edtHoTen: EditText
    private lateinit var edtChucVu: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtSoDienThoai: EditText
    private lateinit var edtTenCongTy: EditText
    private lateinit var edtDiaChiCongTy: EditText
    private lateinit var edtQuyMo: EditText
    private lateinit var btnCapNhat: Button

    private val db = FirebaseFirestore.getInstance()
    private val repository = HoSoNTD_Reponsition()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chinh_sua_ho_so_ntd, container, false)

        edtHoTen = view.findViewById(R.id.edtHoTen)
        edtChucVu = view.findViewById(R.id.edtChucVu)
        edtEmail = view.findViewById(R.id.edtEmail)
        edtSoDienThoai = view.findViewById(R.id.edtSoDienThoai)
        edtTenCongTy = view.findViewById(R.id.edtTenCongTy)
        edtDiaChiCongTy = view.findViewById(R.id.edtDiaChiCongTy)
        edtQuyMo = view.findViewById(R.id.edtQuyMo)
        btnCapNhat = view.findViewById(R.id.btnCapNhat)

        // Nhận dữ liệu từ bundle
        arguments?.let {
            edtHoTen.setText(it.getString("HoTenNtd"))
            edtChucVu.setText(it.getString("ChucVu"))
            edtEmail.setText(it.getString("Email"))
            edtSoDienThoai.setText(it.getString("SoDienThoai"))
            edtTenCongTy.setText(it.getString("TenCongTy"))
            edtDiaChiCongTy.setText(it.getString("DiaChiCongTy"))
            edtQuyMo.setText(it.getString("QuyMo"))
        }

        // Khi nhấn nút cập nhật
        btnCapNhat.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(requireContext(), "Không tìm thấy UID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("FragmentChinhSua", "🔄 Bắt đầu cập nhật...")
                val success = repository.updateNhaTuyenDung(
                    maNTD = uid,
                    hoTen = edtHoTen.text.toString(),
                    chucVu = edtChucVu.text.toString(),
                    email = edtEmail.text.toString(),
                    soDienThoai = edtSoDienThoai.text.toString(),
                    tenCongTy = edtTenCongTy.text.toString(),
                    diaChiCongTy = edtDiaChiCongTy.text.toString(),
                    quyMo = edtQuyMo.text.toString()
                )
                Log.d("FragmentChinhSua", "Kết quả cập nhật = $success")
                if (success) {
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Quay lại trang trước
                } else {
                    Toast.makeText(requireContext(), "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
