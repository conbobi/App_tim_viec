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
import com.example.app_tim_viec.UI.Xac_Thuc.register.EmployerRegisterFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random
import  com.example.app_tim_viec.UI.Xac_Thuc.dangky.FragmentDangKyNTD
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

        // Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Gợi ý tỉnh thành (AutoCompleteTextView)
        val provinces = listOf("Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, provinces)
        autoProvince.setAdapter(adapter)

        // Xử lý nút đăng ký
        btnRegisterEmployer.setOnClickListener { dangKyNhaTuyenDung() }

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

        // Kiểm tra input
        if (email.isEmpty() || password.isEmpty() || companyName.isEmpty() ||
            phone.isEmpty() || registrant.isEmpty() || province.isEmpty() || taxCode.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo tài khoản trên Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val employer = hashMapOf(
                            "id" to uid,
                            "email" to email,
                            "companyName" to companyName,
                            "phone" to phone,
                            "registrant" to registrant,
                            "province" to province,
                            "taxCode" to taxCode
                        )

                        // Lưu thông tin vào Firestore
                        db.collection("employers").document(uid).set(employer)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                Log.d("RegisterEmployer", "Lưu dữ liệu employer thành công")

                                // Gửi email xác thực
                                auth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(context, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.", Toast.LENGTH_LONG).show()
                                        Log.d("RegisterEmployer", "Đã gửi email xác thực")
                                    }
                                    ?.addOnFailureListener { e ->
                                        Toast.makeText(context, "Không gửi được email xác thực: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("RegisterEmployer", "Lỗi gửi email: ${e.message}")
                                    }
                            }

                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Lỗi lưu Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("RegisterEmployer", "Lỗi lưu Firestore: ${e.message}")
                            }
                    }
                } else {
                    Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterEmployer", "Auth thất bại: ${task.exception?.message}")
                }
            }
    }
}