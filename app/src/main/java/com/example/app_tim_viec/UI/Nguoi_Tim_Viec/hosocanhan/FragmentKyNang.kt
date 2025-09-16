package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentKyNangBinding

class FragmentKyNang : Fragment() {

    private var _binding: FragmentKyNangBinding? = null
    private val binding get() = _binding!!

    // Dữ liệu tĩnh: mảng kỹ năng
    private val kyNangList = arrayOf(
        "Java",
        "Kotlin",
        "Android Studio",
        "SQL",
        "React Native",
        "UI/UX Design",
        "Git/GitHub"
    )

    override fun onCreateView(
        //inflater: Dùng để "thổi phồng" (inflate) file XML thành View thật hiển thị trên màn hình.
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKyNangBinding.inflate(inflater, container, false)
        // Adapter cho spinner
        val adapter = ArrayAdapter(
            //requireContext(): lấy context hiện tại (cần để khởi tạo adapter).
            requireContext(),
            //layout mặc định của Android để hiển thị item trong spinner.
            android.R.layout.simple_spinner_dropdown_item,
            kyNangList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //Gán adapter cho Spinner spinnerKyNang (được khai báo trong XML).
        binding.spinnerKyNang.adapter = adapter
        //binding.root chính là gốc (root) của layout trong file XML fragment_ky_nang.xml.
        return binding.root
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}
