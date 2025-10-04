package com.example.app_tim_viec

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.ui.hosocanhan.ActivityHoSoNTV
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_tim_viec.databinding.ActivityMainBinding
import com.example.app_tim_viec.databinding.ActivityManHinhDangNhapBinding
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu.FragmentTrangChuNTV

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManHinhDangNhapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dùng binding thay cho setContentView(R.layout.activity_main)
        binding = ActivityManHinhDangNhapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Áp dụng insets cho layout gốc
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // 🔑 phải return insets
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
