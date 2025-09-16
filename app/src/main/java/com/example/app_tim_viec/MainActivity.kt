package com.example.app_tim_viec

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.ui.hosocanhan.ActivityHoSoNTV
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_tim_viec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dùng binding thay cho setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Áp dụng insets cho layout gốc
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // 🔑 Luôn return lại insets
            insets
        }

        // Xử lý sự kiện cho nút nằm ngoài lambda
        binding.btnOpenChinhSua.setOnClickListener {
            val intent = Intent(this, ActivityHoSoNTV::class.java)
            startActivity(intent)
        }
    }
}
