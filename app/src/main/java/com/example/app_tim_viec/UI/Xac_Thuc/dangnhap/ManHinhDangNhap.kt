package com.example.app_tim_viec.UI.Xac_Thuc.dangnhap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.R

class ManHinhDangNhap : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_dang_nhap)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FragmentDangNhap())
            .commit()
    }
}
