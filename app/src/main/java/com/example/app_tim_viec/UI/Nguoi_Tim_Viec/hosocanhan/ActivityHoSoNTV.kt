package com.example.app_tim_viec.UI.hosocanhan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.MainActivity
import com.example.app_tim_viec.databinding.ActivityHoSoNtvBinding
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ActivityEditProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.app_tim_viec.R
import android.app.ActivityOptions


class ActivityHoSoNTV : AppCompatActivity() {
    private lateinit var binding: ActivityHoSoNtvBinding

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHoSoNtvBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Load họ tên và các thông tin tạm thời ---
        loadUserProfile()

        // Nút Home quay về MainActivity
        binding.ivHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "TrangChuNTV")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent, options.toBundle()) // 🧩 Thay thế overridePendingTransition
            finish()
        }

        // 👉 Nút sửa hồ sơ
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }
    }

    // Khi quay lại từ EditProfile, reload thông tin
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    // --------------------- Hàm load dữ liệu người dùng ---------------------
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (userDoc != null && userDoc.exists()) {
                    binding.txtName.text = userDoc.getString("hoTen") ?: "Chưa cập nhật"
                    binding.txtJobTitle.text = userDoc.getString("jobTitle") ?: "Chưa cập nhật"
                    binding.txtLocation.text = userDoc.getString("location") ?: "Chưa cập nhật"

                    // --- thêm: load avatar ---
                    val avatarUrl = userDoc.getString("avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(binding.imgAvatar)
                    } else {
                        binding.imgAvatar.setImageResource(R.drawable.sample_avatar)
                    }
                }
            }
            .addOnFailureListener {
                binding.txtName.text = "Lỗi tải dữ liệu"
                binding.txtJobTitle.text = "Chưa cập nhật"
                binding.txtLocation.text = "Chưa cập nhật"
            }
    }

}
