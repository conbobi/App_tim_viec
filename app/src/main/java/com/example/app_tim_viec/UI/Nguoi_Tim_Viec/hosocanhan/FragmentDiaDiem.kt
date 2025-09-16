package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentDiaDiemBinding

class FragmentDiaDiem : Fragment() {

    private var _binding: FragmentDiaDiemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaDiemBinding.inflate(inflater, container, false)

        // Danh sách địa điểm tĩnh
        val diaDiem = arrayOf("Hà Nội", "Hải Phòng", "Huế", "Hà Giang", "Hậu Giang",
                            "Bình Dương", "Bình Định","Bình Phước" )

        // Tạo adapter cho AutoCompleteTextView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
                                    diaDiem)
        binding.autoCompleteDiaDiem.setAdapter(adapter)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
