// file: ActivityEditProfile.kt
package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.net.Uri
import com.example.app_tim_viec.R
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.app_tim_viec.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityEditProfile : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    // --- thêm: launcher để chọn ảnh từ gallery ---
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init ActivityResultLauncher
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                // preview ảnh local ngay
                binding.imgAvatarEdit.setImageURI(it)
                // upload lên Cloudinary
                uploadImageToCloudinary(it)
            }
        }

        // Khi click vào avatar trong màn edit -> chọn ảnh
        binding.imgAvatarEdit.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Load dữ liệu hiện có
        loadProfile()

        // Save (các trường text)
        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun loadProfile() {
        uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        binding.edtName.setText(doc.getString("hoTen") ?: "")
                        binding.edtJobTitle.setText(doc.getString("jobTitle") ?: "")
                        binding.edtLocation.setText(doc.getString("location") ?: "")

                        // --- thêm: load avatar nếu có ---
                        val avatarUrl = doc.getString("avatarUrl")
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop() // vẽ tròn
                                .into(binding.imgAvatarEdit)
                        } else {
                            binding.imgAvatarEdit.setImageResource(R.drawable.sample_avatar)
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy hồ sơ", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi khi load hồ sơ", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
        val name = binding.edtName.text.toString().trim()
        val jobTitle = binding.edtJobTitle.text.toString().trim()
        val location = binding.edtLocation.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "hoTen" to name,
            "jobTitle" to jobTitle,
            "location" to location
        )

        uid?.let { uid ->
            db.collection("users").document(uid)
                .update(data as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Hàm upload lên Cloudinary (unsigned preset) ---
    private fun uploadImageToCloudinary(imageUri: Uri) {
        // hiển thị toast / progress đơn giản
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
        val preset = getString(R.string.cloudinary_upload_preset) // từ strings.xml
        // Optionally set folder and resource_type
        MediaManager.get().upload(imageUri)
            .unsigned(preset)
            .option("folder", "avatars")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // bạn có thể cập nhật progress bar nếu muốn
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Log.d("Cloudinary", "Upload success: $resultData")
                    // secure_url thường chứa link HTTPS
                    val url = resultData?.get("secure_url") as? String ?: resultData?.get("url") as? String
                    if (!url.isNullOrEmpty()) {
                        // Lưu URL vào Firestore
                        uid?.let { uid ->
                            db.collection("users").document(uid)
                                .update("avatarUrl", url)
                                .addOnSuccessListener {
                                    Toast.makeText(this@ActivityEditProfile, "Ảnh đã được cập nhật", Toast.LENGTH_SHORT).show()
                                    // show ảnh mới
                                    Glide.with(this@ActivityEditProfile)
                                        .load(url)
                                        .circleCrop()
                                        .into(binding.imgAvatarEdit)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@ActivityEditProfile, "Lưu ảnh lên Firestore thất bại", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this@ActivityEditProfile, "Không lấy được URL ảnh", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload error: ${error?.description}")
                    Toast.makeText(this@ActivityEditProfile, "Tải ảnh thất bại: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("Cloudinary", "Rescheduled: ${error?.description}")
                }
            })
            .dispatch()
    }
}
