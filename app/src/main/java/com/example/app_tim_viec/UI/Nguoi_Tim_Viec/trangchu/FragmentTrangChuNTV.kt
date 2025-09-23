package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R
import com.example.app_tim_viec.ui.hosocanhan.ActivityHoSoNTV

class FragmentTrangChuNTV : Fragment() {
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
    }
}
