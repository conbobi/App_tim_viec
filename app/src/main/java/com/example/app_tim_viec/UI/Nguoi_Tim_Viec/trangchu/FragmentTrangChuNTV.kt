package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu

import android.content.Intent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentTrangChuNtvBinding
import com.example.app_tim_viec.UI.hosocanhan.ActivityHoSoNTV

import com.example.app_tim_viec.R
import com.example.app_tim_viec.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.example.app_tim_viec.ui.hosocanhan.ActivityHoSoNTV

class FragmentTrangChuNTV : Fragment() {
    private var _binding: FragmentTrangChuNtvBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trang_chu_ntv, container, false)

        // Tìm nút Hồ sơ
        val btnHoSo = view.findViewById<ImageView>(R.id.icHoso)

        // Gán sự kiện click
        btnHoSo.setOnClickListener {
            val intent = Intent(requireContext(), ActivityHoSoNTV::class.java)
            startActivity(intent)
        }

        return view
    ): View {
        _binding = FragmentTrangChuNtvBinding.inflate(inflater, container, false)

        // Khi đang ở Trang chủ thì đánh dấu icon Trang chủ là active
        binding.ivHoSo.isSelected = true

        // Bắt sự kiện click để mở Activity hồ sơ
        binding.ivHoSo.setOnClickListener {
            val intent = Intent(requireContext(), ActivityHoSoNTV::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(0, 0) // 🚀 Tắt animation chuyển trang
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // tránh leak memory
    }
}
