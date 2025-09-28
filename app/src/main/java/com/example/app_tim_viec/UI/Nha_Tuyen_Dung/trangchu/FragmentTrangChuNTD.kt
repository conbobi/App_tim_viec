package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.trangchu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nha_Tuyen_Dung.dangtintuyendung.FragmentQLTinTuyenDung
import com.example.app_tim_viec.databinding.FragmentTrangChuNtdBinding

class FragmentTrangChuNTD : Fragment() {

    private var _binding: FragmentTrangChuNtdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("FragmentTrangChuNTD", "onCreateView: bắt đầu inflate layout")

        try {
            _binding = FragmentTrangChuNtdBinding.inflate(inflater, container, false)
            Log.d("FragmentTrangChuNTD", "onCreateView: binding inflate OK")

            // Bắt sự kiện click
            binding.btnJobs.setOnClickListener {
                Log.d("FragmentTrangChuNTD", "btnJobs được click")
                try {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FragmentQLTinTuyenDung())
                        .addToBackStack(null)
                        .commit()
                    Log.d("FragmentTrangChuNTD", "Chuyển sang FragmentQLTinTuyenDung thành công")
                } catch (e: Exception) {
                    Log.e("FragmentTrangChuNTD", "Lỗi khi replace fragment: ${e.message}", e)
                }
            }

        } catch (e: Exception) {
            Log.e("FragmentTrangChuNTD", "Lỗi khi inflate layout hoặc setOnClick: ${e.message}", e)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentTrangChuNTD", "onDestroyView: binding bị hủy")
        _binding = null
    }
}
