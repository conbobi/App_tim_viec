package com.example.app_tim_viec.UI.Nha_Tuyen_Dung.hosocongty

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Xac_Thuc.dangnhap.ManHinhDangNhap // Import Activity Đăng nhập
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentHoSoNTD : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var imgAvatar: ImageView
    private lateinit var txtHoTen: TextView
    private lateinit var txtChucVu: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtSoDienThoai: TextView
    private lateinit var txtTenCongTy: TextView
    private lateinit var txtDiaChiCongTy: TextView
    private lateinit var txtQuyMo: TextView
    private lateinit var btnChinhSua: Button
    private lateinit var btnDangXuat: TextView

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImageToCloudinary(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ho_so_ntd, container, false)

        // 1. Ánh xạ view
        imgAvatar = view.findViewById(R.id.imgAvatar)
        txtHoTen = view.findViewById(R.id.txtHoTen)
        txtChucVu = view.findViewById(R.id.txtChucVu)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtSoDienThoai = view.findViewById(R.id.txtSoDienThoai)
        txtTenCongTy = view.findViewById(R.id.txtTenCongTy)
        txtDiaChiCongTy = view.findViewById(R.id.txtDiaChiCongTy)
        txtQuyMo = view.findViewById(R.id.txtQuyMo)
        btnChinhSua = view.findViewById(R.id.btnChinhSua)
        btnDangXuat = view.findViewById(R.id.btnDangXuat)

        // 2. Bắt sự kiện click
        imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 3. Load dữ liệu
        loadUserData()

        // 4. Xử lý nút Chỉnh sửa
        btnChinhSua.setOnClickListener {
            val bundle = Bundle().apply {
                putString("HoTenNtd", txtHoTen.text.toString())
                putString("ChucVu", txtChucVu.text.toString())
                putString("Email", txtEmail.text.toString().removePrefix("Email: ")) // Xóa prefix nếu có
                putString("SoDienThoai", txtSoDienThoai.text.toString().removePrefix("SĐT: "))
                putString("TenCongTy", txtTenCongTy.text.toString().removePrefix("Tên công ty: "))
                putString("DiaChiCongTy", txtDiaChiCongTy.text.toString().removePrefix("Địa chỉ công ty: "))
                putString("QuyMo", txtQuyMo.text.toString().removePrefix("Quy mô: "))
            }

            val fragment = FragmentChinhSuaHoSoNTD()
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // 🔥 SỬA LẠI ID container cho đúng
                .addToBackStack(null)
                .commit()
        }

        // 5. Xử lý Đăng xuất (Code chuẩn)
        btnDangXuat.setOnClickListener {
            auth.signOut() // Đăng xuất khỏi Firebase
            Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

            // Chuyển về Activity Đăng nhập
            val intent = Intent(requireActivity(), ManHinhDangNhap::class.java)
            // Xóa cờ activity để không back lại được
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish() // Đóng Activity hiện tại
        }

        return view
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            // Nếu không có user (ví dụ session hết hạn), quay về đăng nhập
            val intent = Intent(requireContext(), ManHinhDangNhap::class.java)
            startActivity(intent)
            requireActivity().finish()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Lấy dữ liệu an toàn (dùng elvis operator ?: để tránh null)
                    val registrant = document.getString("registrant") ?: "Chưa cập nhật tên"
                    val email = document.getString("email") ?: "Chưa cập nhật email"
                    val phone = document.getString("phone") ?: "Chưa cập nhật sđt"
                    val companyName = document.getString("companyName") ?: "Chưa cập nhật tên cty"
                    val province = document.getString("province") ?: "Chưa cập nhật địa chỉ"
                    val chucVu = document.getString("chucVu") ?: "Nhà tuyển dụng"
                    val quyMo = document.getString("quyMo") ?: "Chưa cập nhật quy mô"
                    val avatarUrl = document.getString("avatarUrl")

                    // Gán lên giao diện
                    txtHoTen.text = registrant
                    txtChucVu.text = chucVu
                    txtEmail.text = email // Layout XML đã có icon/label rồi nên không cần "Email: "
                    txtSoDienThoai.text = phone
                    txtTenCongTy.text = companyName
                    txtDiaChiCongTy.text = province
                    txtQuyMo.text = quyMo

                    // Load ảnh
                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .into(imgAvatar)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
        val preset = getString(R.string.cloudinary_upload_preset)

        MediaManager.get().upload(imageUri)
            .unsigned(preset)
            .option("folder", "avatars")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String ?: resultData?.get("url") as? String
                    if (!url.isNullOrEmpty()) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            db.collection("users").document(uid)
                                .update("avatarUrl", url)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ảnh đã được cập nhật", Toast.LENGTH_SHORT).show()
                                    Glide.with(this@FragmentHoSoNTD).load(url).placeholder(R.drawable.ic_avatar_placeholder).into(imgAvatar)
                                }
                        }
                    }
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Tải ảnh thất bại: ${error?.description}", Toast.LENGTH_LONG).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}