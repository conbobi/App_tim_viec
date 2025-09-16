package com.example.app_tim_viec.ui.hosocanhan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.databinding.ActivityChinhSuaHoSoBinding
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.FragmentDiaDiem
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.FragmentKyNang
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.FragmentNgayThang
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.Fragment_Gio
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.Fragment_ChinhSuaKiNang
class ActivityChinhSuaHoSo : AppCompatActivity() {

    private lateinit var binding: ActivityChinhSuaHoSoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChinhSuaHoSoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ví dụ TextView
        binding.txtEdit.text = "Đây là màn hình chỉnh sửa hồ sơ"
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerDiaDiem, FragmentDiaDiem())
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerKyNang, FragmentKyNang())
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerNgayThang, FragmentNgayThang())
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerGio, Fragment_Gio())
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerChinhSuaKyNang, Fragment_ChinhSuaKiNang())
            .commit()
    }

}
