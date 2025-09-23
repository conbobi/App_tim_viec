package com.example.app_tim_viec.UI.Xac_Thuc.dangnhap

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
import com.example.app_tim_viec.UI.Xac_Thuc.dangky.FragmentDangKyNTV
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu.FragmentTrangChuNTV
import com.google.firebase.auth.FirebaseAuth

class FragmentDangNhap : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dang_nhap, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)

        auth = FirebaseAuth.getInstance()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Đăng nhập với FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)

                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        if (uid != null) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val role = document.getString("role") // <-- lấy role từ Firestore
                                        if (role == "employer") {
                                            Toast.makeText(requireContext(), "Đăng nhập thành công (NTD)!", Toast.LENGTH_SHORT).show()
                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.fragmentContainer, com.example.app_tim_viec.UI.Nha_Tuyen_Dung.trangchu.FragmentTrangChuNTD())
                                                .commit()
                                        } else if (role == "nguoi tim viec") {
                                            Toast.makeText(requireContext(), "Đăng nhập thành công (NTV)!", Toast.LENGTH_SHORT).show()
                                            requireActivity().supportFragmentManager.beginTransaction()
                                                .replace(R.id.fragmentContainer, com.example.app_tim_viec.UI.Nguoi_Tim_Viec.trangchu.FragmentTrangChuNTV())
                                                .commit()
                                        } else {
                                            Toast.makeText(requireContext(), "Tài khoản không có role hợp lệ!", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Log.e("Login", "Không tìm thấy thông tin user trong Firestore")
                                        Toast.makeText(requireContext(), "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Login", "Lỗi đọc Firestore: ${e.message}")
                                    Toast.makeText(requireContext(), "Lỗi hệ thống!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.e("Login", "Đăng nhập thất bại: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvRegister.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentDangKyNTV())
                .addToBackStack(null)
                .commit()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng quên mật khẩu đang phát triển", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
