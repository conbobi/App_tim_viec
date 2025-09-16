package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentNgayThangBinding
import java.util.Calendar

class FragmentNgayThang : Fragment() {

    private var _binding: FragmentNgayThangBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNgayThangBinding.inflate(inflater, container, false)

        // Khi click vào EditText -> mở DatePicker
        binding.edtNgaySinh.setOnClickListener {
            showDatePicker()
        }
        return binding.root
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val ngaySinh = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.edtNgaySinh.setText(ngaySinh)
                Toast.makeText(requireContext(), "Ngày sinh: $ngaySinh", Toast.LENGTH_SHORT).show()
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
