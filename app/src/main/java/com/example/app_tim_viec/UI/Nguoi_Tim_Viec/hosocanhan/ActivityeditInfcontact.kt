package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_tim_viec.databinding.ActivityEditInfcontactBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityeditInfcontact : AppCompatActivity() {

    private lateinit var binding: ActivityEditInfcontactBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfcontactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileToForm()

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfileToForm() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    binding.etEmail.setText(doc.getString("email") ?: auth.currentUser?.email ?: "")
                    binding.etPhone.setText(doc.getString("soDienThoai") ?: auth.currentUser?.phoneNumber ?: "")
                    binding.etAddress.setText(doc.getString("DiaChiNha") ?: "")
                } else {
                    binding.etEmail.setText(auth.currentUser?.email ?: "")
                    binding.etPhone.setText(auth.currentUser?.phoneNumber ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải thông tin: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newEmail = binding.etEmail.text.toString().trim()
        val newPhone = binding.etPhone.text.toString().trim()
        val newAddress = binding.etAddress.text.toString().trim()

        val updates = hashMapOf<String, Any>()
        if (newPhone.isNotEmpty()) updates["soDienThoai"] = newPhone
        if (newAddress.isNotEmpty()) updates["DiaChiNha"] = newAddress

        val currentAuthEmail = auth.currentUser?.email ?: ""

        if (newEmail.isNotEmpty() && newEmail != currentAuthEmail) {
            auth.currentUser?.updateEmail(newEmail)
                ?.addOnSuccessListener {
                    updates["email"] = newEmail
                    updateFirestore(uid, updates)
                }
                ?.addOnFailureListener { ex ->
                    updates["email"] = newEmail
                    updateFirestore(uid, updates)
                    val msg = when (ex) {
                        is FirebaseNetworkException -> "Lỗi mạng, thử lại."
                        else -> "Đã lưu email vào hồ sơ. Để đổi email trong Firebase Auth có thể cần đăng nhập lại."
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        } else {
            if (newEmail.isNotEmpty()) updates["email"] = newEmail
            if (updates.isEmpty()) {
                Toast.makeText(this, "Không có gì thay đổi", Toast.LENGTH_SHORT).show()
                return
            }
            updateFirestore(uid, updates)
        }
    }

    private fun updateFirestore(uid: String, updates: HashMap<String, Any>) {
        db.collection("users").document(uid)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lưu thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
