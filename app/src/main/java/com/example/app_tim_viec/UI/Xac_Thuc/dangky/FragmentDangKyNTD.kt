package com.example.app_tim_viec.UI.Xac_Thuc.dangky
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R

import android.widget.ArrayAdapter
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.app_tim_viec.UI.Xac_Thuc.dangnhap.FragmentDangNhap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentDangKyNTD : Fragment() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etCompanyName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etRegistrant: EditText
    private lateinit var autoProvince: AutoCompleteTextView
    private lateinit var etTaxCode: EditText
    private lateinit var btnRegisterEmployer: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var tvBackToLogin: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dang_ky_ntd, container, false)

        // Ánh xạ view
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etCompanyName = view.findViewById(R.id.etCompanyName)
        etPhone = view.findViewById(R.id.etPhone)
        etRegistrant = view.findViewById(R.id.etRegistrant)
        autoProvince = view.findViewById(R.id.autoProvince)
        etTaxCode = view.findViewById(R.id.etTaxCode)
        btnRegisterEmployer = view.findViewById(R.id.btnRegisterEmployer)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)
        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Gợi ý tỉnh thành (AutoCompleteTextView)
        val provinces = listOf("Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, provinces)
        autoProvince.setAdapter(adapter)

        // Xử lý nút đăng ký
        btnRegisterEmployer.setOnClickListener { dangKyNhaTuyenDung() }
//xử lý quay trở về đăng nhập
        tvBackToLogin.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentDangNhap())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    private fun dangKyNhaTuyenDung() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val companyName = etCompanyName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val registrant = etRegistrant.text.toString().trim()
        val province = autoProvince.text.toString().trim()
        val taxCode = etTaxCode.text.toString().trim()

        Log.d("RegisterEmployer", "Bắt đầu đăng ký với email=$email, company=$companyName, phone=$phone")

        // Kiểm tra input
        if (email.isEmpty() || password.isEmpty() || companyName.isEmpty() ||
            phone.isEmpty() || registrant.isEmpty() || province.isEmpty() || taxCode.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            Log.e("RegisterEmployer", "Thiếu thông tin input")
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
            Log.e("RegisterEmployer", "Mật khẩu quá ngắn")
            return
        }

        // Tạo tài khoản trên Firebase Auth
        Log.d("RegisterEmployer", "Gọi createUserWithEmailAndPassword...")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterEmployer", "Tạo user Auth thành công")
                    val user = task.result?.user
                    val uid = user?.uid
                    Log.d("RegisterEmployer", "UID được tạo: $uid")

                    if (uid != null) {
                        val userData = hashMapOf(
                            "id" to uid,
                            "email" to email,
                            "role" to "employer",   // 👈 thêm role
                            "companyName" to companyName,
                            "phone" to phone,
                            "registrant" to registrant,
                            "province" to province,
                            "taxCode" to taxCode
                        )

                        Log.d("RegisterEmployer", "Bắt đầu lưu Firestore vào collection 'users' với data: $userData")

                        db.collection("users").document(uid).set(userData)  // 👈 dùng 'users'
                            .addOnSuccessListener {
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                Log.d("RegisterEmployer", "Lưu dữ liệu user thành công")

                                // Gửi email xác thực
                                Log.d("RegisterEmployer", "Bắt đầu gửi email xác thực...")
                                user.sendEmailVerification()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Vui lòng kiểm tra email để xác thực.", Toast.LENGTH_LONG).show()
                                        Log.d("RegisterEmployer", "Đã gửi email xác thực thành công tới: ${user.email}")
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Không gửi được email xác thực: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("RegisterEmployer", "Lỗi gửi email: ${e.message}", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Lỗi lưu Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("RegisterEmployer", "Lỗi lưu Firestore: ${e.message}", e)
                            }
                    }

                    else {
                        Log.e("RegisterEmployer", "UID null sau khi tạo tài khoản")
                    }
                } else {
                    Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterEmployer", "Auth thất bại: ${task.exception?.message}", task.exception)
                }
            }
    }

}