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
        // BƯỚC 1: UI - Thông báo cho người dùng biết quá trình bắt đầu
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()

        // Lấy cấu hình (preset) từ file strings.xml
        val preset = getString(R.string.cloudinary_upload_preset)

        // BƯỚC 2: CLOUDINARY REQUEST - Thực hiện đẩy file ảnh từ máy lên Server Cloudinary
        MediaManager.get().upload(imageUri)
            .unsigned(preset) // Chế độ upload không cần đăng nhập (unsigned)
            .option("folder", "avatars") // Chỉ định lưu vào thư mục 'avatars' trên Cloud
            .option("resource_type", "image") // Khai báo đây là file ảnh
            .callback(object : UploadCallback { // Lắng nghe kết quả trả về (Callback)

                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Bắt đầu upload...")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Chỗ này dùng để cập nhật thanh loading % nếu cần
                }

                // BƯỚC 3: CLOUDINARY RESPONSE - Upload thành công, Server trả về kết quả
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Log.d("Cloudinary", "Upload thành công: $resultData")

                    // BƯỚC 4: TRÍCH XUẤT DỮ LIỆU - Lấy đường Link ảnh (URL) từ kết quả trả về
                    // 'secure_url' là link https (bảo mật), nếu không có thì lấy 'url' thường
                    val url = resultData?.get("secure_url") as? String ?: resultData?.get("url") as? String

                    if (!url.isNullOrEmpty()) {
                        // BƯỚC 5: FIREBASE REQUEST - Có link rồi -> Gọi Firebase để lưu link vào DB
                        uid?.let { uid ->
                            db.collection("users").document(uid)
                                .update("avatarUrl", url) // Chỉ update trường 'avatarUrl', giữ nguyên tên/tuổi

                                // BƯỚC 6: FIREBASE RESPONSE - Database báo đã lưu xong
                                .addOnSuccessListener {
                                    Toast.makeText(this@ActivityEditProfile, "Ảnh đã được cập nhật", Toast.LENGTH_SHORT).show()

                                    // BƯỚC 7: UI UPDATE - Dùng thư viện Glide tải ảnh từ Link mới về hiển thị ngay
                                    Glide.with(this@ActivityEditProfile)
                                        .load(url)
                                        .circleCrop() // Cắt ảnh hình tròn
                                        .into(binding.imgAvatarEdit)
                                }
                                .addOnFailureListener {
                                    // Lỗi khi lưu vào Firebase
                                    Toast.makeText(this@ActivityEditProfile, "Lưu ảnh lên Firestore thất bại", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this@ActivityEditProfile, "Không lấy được URL ảnh từ Cloudinary", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    // Xử lý lỗi khi không upload được lên Cloudinary (mạng yếu, sai key...)
                    Log.e("Cloudinary", "Lỗi Upload: ${error?.description}")
                    Toast.makeText(this@ActivityEditProfile, "Tải ảnh thất bại: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("Cloudinary", "Rescheduled: ${error?.description}")
                }
            })
            .dispatch() // Lệnh kích hoạt ("bắn") request đi
    }
}
