package com.example.app_tim_viec

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_tim_viec.databinding.ActivityManHinhDangNhapBinding
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu.FragmentTrangChuNTV

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManHinhDangNhapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManHinhDangNhapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Reset padding rồi mới set lại
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // ✅ Khi nhận intent quay về từ Hồ sơ, load lại fragment đúng
        val openFragment = intent.getStringExtra("openFragment")
        if (openFragment == "TrangChuNTV") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, FragmentTrangChuNTV())
                .commitAllowingStateLoss()
        }
    }
}
