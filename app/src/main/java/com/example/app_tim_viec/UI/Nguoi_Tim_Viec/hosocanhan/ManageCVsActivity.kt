package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.databinding.ActivityManageCvsBinding
import com.example.app_tim_viec.UI.Nguoi_Tim_Viec.hosocanhan.SupabaseModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

// Bạn cần tạo file model data class
data class CVModel(
    val docId: String = "", // ID của document trong Firestore
    val userId: String = "",
    val fileName: String = "",
    val storagePath: String = "",
    val downloadUrl: String = ""
)

// File ManageCVsActivity.kt
class ManageCVsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageCvsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val supabaseStorage = SupabaseModule.client.storage
    private lateinit var cvAdapter: CVAdapter // Bạn sẽ cần tự tạo Adapter này
    private val cvList = mutableListOf<CVModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCvsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.fabAddCV.setOnClickListener {
            // Mở màn hình Upload
            val intent = Intent(this, UploadCVActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCVsFromFirestore() // Tải lại danh sách khi quay về
    }

    private fun setupRecyclerView() {
        cvAdapter = CVAdapter(cvList,
            onDeleteClick = { cv ->
                deleteCV(cv)
            }
        )
        binding.rvCVs.layoutManager = LinearLayoutManager(this)
        binding.rvCVs.adapter = cvAdapter
    }

    private fun loadCVsFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        // 📌 SỬA LỖI 1: Đọc từ collection "CVS" (viết hoa)
        db.collection("CVS")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                cvList.clear()
                for (doc in documents) {
                    val cv = doc.toObject(CVModel::class.java).copy(docId = doc.id)
                    cvList.add(cv)
                }
                cvAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải danh sách CV", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCV(cv: CVModel) {
        lifecycleScope.launch {
            try {
                // 📌 SỬA LỖI 2: Xóa file từ bucket "CVS" (viết hoa)
                supabaseStorage.from("CVS").delete(cv.storagePath)

                // 📌 SỬA LỖI 3: Xóa document từ collection "CVS" (viết hoa)
                db.collection("CVS").document(cv.docId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@ManageCVsActivity, "Đã xóa CV", Toast.LENGTH_SHORT).show()
                        loadCVsFromFirestore() // Tải lại danh sách
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ManageCVsActivity, "Lỗi xóa (DB)", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                Toast.makeText(this@ManageCVsActivity, "Lỗi xóa (Storage): ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}