package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.databinding.FragmentGioBinding
import java.util.Calendar

class Fragment_Gio : Fragment() {

    private var _binding: FragmentGioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGioBinding.inflate(inflater, container, false)

        // Khi click vào EditText -> mở TimePicker
        binding.edtGio.setOnClickListener {
            showTimePicker()
        }

        return binding.root
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val gioChon = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.edtGio.setText(gioChon)
                Toast.makeText(requireContext(), "Giờ đã chọn: $gioChon", Toast.LENGTH_SHORT).show()
            },
            hour, minute, true // true = định dạng 24h
        )
        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
