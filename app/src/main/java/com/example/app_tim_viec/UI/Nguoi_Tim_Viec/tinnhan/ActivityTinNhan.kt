package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.tinnhan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
// 🔥 IMPORT QUAN TRỌNG: Binding tự sinh ra từ activity_tin_nhan.xml
import com.example.app_tim_viec.databinding.ActivityTinNhanBinding
import com.example.app_tim_viec.UI.Chat.ChatActivity
import com.example.app_tim_viec.models.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ActivityTinNhan : AppCompatActivity() {

    // 🔥 Khai báo biến Binding thay vì khai báo từng View lẻ tẻ
    private lateinit var binding: ActivityTinNhanBinding

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 Cài đặt Binding (Thay thế setContentView R.layout...)
        binding = ActivityTinNhanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sử dụng binding để gọi view (Không cần findViewById nữa)
        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        loadChatRooms()
    }

    private fun setupRecyclerView() {
        adapter = ChatRoomAdapter(emptyList()) { otherId, otherName ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userId", otherId)
            intent.putExtra("userName", otherName)
            startActivity(intent)
        }

        // 🔥 Gọi Recycler qua binding
        binding.recyclerTinNhan.layoutManager = LinearLayoutManager(this)
        binding.recyclerTinNhan.adapter = adapter
    }

    private fun loadChatRooms() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE // 🔥 Gọi ProgressBar qua binding

        db.collection("Chats")
            .whereArrayContains("participants", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) return@addSnapshotListener

                if (value != null && !value.isEmpty) {
                    val list = value.toObjects(ChatRoom::class.java)
                    adapter.updateData(list)
                    binding.tvEmpty.visibility = View.GONE // 🔥 Gọi TextView qua binding
                } else {
                    binding.tvEmpty.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                }
            }
    }
}