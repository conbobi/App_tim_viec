package com.example.app_tim_viec.UI.hosocanhan

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.app_tim_viec.MainActivity
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ActivityEditProfile
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.ActivityeditInfcontact
import com.example.app_tim_viec.databinding.ActivityHoSoNtvBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.app_tim_viec.UI.Xac_Thuc.dangnhap.ManHinhDangNhap// ⚠️ chỉnh theo Activity thật của bạn chứa FragmentDangNhap



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
            startActivity(intent, options.toBundle())
            finish()
        }

        // Nút sửa toàn bộ hồ sơ (nút lớn)
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }

        //  Thêm đoạn này ngay dưới đây:
        binding.btnEditInfcontact.setOnClickListener {
            val intent = Intent(this, ActivityeditInfcontact::class.java)
            startActivity(intent)
        }

        // Nút sửa từng trường (phone, email, address) -> mở ActivityEditProfile và focus vào trường tương ứng
        binding.ivEditPhone.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            intent.putExtra("editField", "phone")
            startActivity(intent)
        }
        binding.ivEditEmail.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            intent.putExtra("editField", "email")
            startActivity(intent)
        }
        binding.ivEditAddress.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            intent.putExtra("editField", "address")
            startActivity(intent)
        }


        // --- Xử lý nút đăng xuất ---
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

            // Quay về màn hình đăng nhập (Activity chứa FragmentDangNhap)
            val intent = Intent(this, ManHinhDangNhap::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // reload data khi quay lại
        loadUserProfile()
    }

    // --------------------- Hàm load dữ liệu người dùng ---------------------
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (userDoc != null && userDoc.exists()) {
                    // Name / job / location (giữ như trước)
                    binding.txtName.text = userDoc.getString("hoTen") ?: "Chưa cập nhật"
                    binding.txtJobTitle.text = userDoc.getString("jobTitle") ?: "Chưa cập nhật"
                    binding.txtLocation.text = userDoc.getString("location") ?: "Chưa cập nhật"

                    // Email: ưu tiên lưu trong doc, nếu không có lấy từ FirebaseAuth
                    val emailFromDoc = userDoc.getString("email")
                    binding.txtEmail.text = emailFromDoc ?: (auth.currentUser?.email ?: "Chưa cập nhật")

                    // Phone: ưu tiên doc, fallback auth phoneNumber (nếu dùng phone auth)
                    val phoneFromDoc = userDoc.getString("soDienThoai")
                    binding.txtPhone.text = phoneFromDoc ?: (auth.currentUser?.phoneNumber ?: "Chưa cập nhật")

                    // Address (DiaChiNha)
                    binding.txtAddress.text = userDoc.getString("DiaChiNha") ?: "Chưa cập nhật"

                    // Avatar
                    val avatarUrl = userDoc.getString("avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(avatarUrl)
                            .circleCrop()
                            .into(binding.imgAvatar)
                    } else {
                        binding.imgAvatar.setImageResource(R.drawable.sample_avatar)
                    }
                } else {
                    // doc không tồn tại
                    binding.txtName.text = "Chưa cập nhật"
                    binding.txtJobTitle.text = "Chưa cập nhật"
                    binding.txtLocation.text = "Chưa cập nhật"
                    binding.txtEmail.text = auth.currentUser?.email ?: "Chưa cập nhật"
                    binding.txtPhone.text = auth.currentUser?.phoneNumber ?: "Chưa cập nhật"
                    binding.txtAddress.text = "Chưa cập nhật"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
