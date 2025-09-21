package com.example.app_tim_viec.UI.Xac_Thuc.dangky

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app_tim_viec.R
import com.example.app_tim_viec.UI.Xac_Thuc.dangnhap.FragmentDangNhap
import com.example.app_tim_viec.UI.Xac_Thuc.register.EmployerRegisterFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random
import  com.example.app_tim_viec.UI.Xac_Thuc.dangky.FragmentDangKyNTD
class FragmentDangKyNTV : Fragment() {

    //nút qua trang đăng kí
    private lateinit var  btnRegisterEmployer: Button

    private lateinit var etHo: EditText
    private lateinit var etTen: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSdt: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvBackToLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dang_ky_ntv, container, false)

        etHo = view.findViewById(R.id.etHo)
        etTen = view.findViewById(R.id.etTen)
        etEmail = view.findViewById(R.id.etEmail)
        etSdt = view.findViewById(R.id.etSdt)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnRegister.setOnClickListener { dangKyNguoiDung() }
// đăng kí nhà tuyển dụng
        btnRegisterEmployer = view.findViewById(R.id.btnRegisterEmployer)
// Đăng ký NTV
        btnRegister.setOnClickListener { dangKyNguoiDung() }

        btnRegisterEmployer.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentDangKyNTD())
                .addToBackStack(null)
                .commit()
        }
        tvBackToLogin.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentDangNhap())
                .addToBackStack(null)
                .commit()
        }

        return view
    }


    private fun dangKyNguoiDung() {
        val ho = etHo.text.toString().trim()
        val ten = etTen.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val sdt = etSdt.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 1. Kiểm tra rỗng
        if (ho.isEmpty() || ten.isEmpty() || email.isEmpty() || sdt.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Kiểm tra định dạng email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Email không hợp lệ!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Kiểm tra số điện thoại (10 số)
        if (!sdt.matches(Regex("^[0-9]{10}$"))) {
            Toast.makeText(context, "Số điện thoại phải gồm 10 chữ số!", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Kiểm tra mật khẩu tối thiểu 6 ký tự
        if (password.length < 6) {
            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. Kiểm tra mật khẩu nhập lại
        if (password != confirmPassword) {
            Toast.makeText(context, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = "$ho $ten"

        // Nếu tất cả đều OK thì đăng ký
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "id" to uid,
                            "hoTen" to fullName,
                            "email" to email,
                            "soDienThoai" to sdt
                        )

                        db.collection("users").document(uid).set(user)
                            .addOnSuccessListener {
                                Log.d("Register", "Lưu thành công cả Auth và Firestore")
                                Toast.makeText(
                                    context,
                                    "Đăng ký thành công và lưu dữ liệu!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Register", "Auth thành công nhưng Firestore thất bại: ${e.message}")
                                Toast.makeText(
                                    context,
                                    "Đăng ký thành công nhưng lỗi lưu Firestore",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Log.e("Register", "Auth thất bại: ${task.exception?.message}")
                    Toast.makeText(
                        context,
                        "Đăng ký thất bại: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }



}
