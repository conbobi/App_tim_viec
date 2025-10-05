package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.tinnhan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.MainActivity
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.hosocanhan.ActivityHoSoNTV
import com.example.app_tim_viec.databinding.ActivityTinNhanBinding

class ActivityTinNhan : AppCompatActivity() {
    private lateinit var binding: ActivityTinNhanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTinNhanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Gắn Fragment danh sách tin nhắn vào khung hiển thị
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FragmentDanhSachTN())
            .commit()

        // ✅ Xử lý sự kiện các icon menu
        binding.ivTrangChu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "TrangChuNTV")
            startActivity(intent)
            finish()
        }

        binding.ivHoSo.setOnClickListener {
            val intent = Intent(this, ActivityHoSoNTV::class.java)
            startActivity(intent)
        }

        // Chat hiện tại -> không cần xử lý
        binding.ivChat.isSelected = true

        // ✅ Xử lý tìm kiếm (nếu có)
        binding.edtSearchChat.setOnEditorActionListener { _, _, _ ->
            val keyword = binding.edtSearchChat.text.toString().trim()
            if (keyword.isNotEmpty()) {
                // TODO: thêm logic tìm kiếm tin nhắn ở đây
            }
            true
        }
    }
}
