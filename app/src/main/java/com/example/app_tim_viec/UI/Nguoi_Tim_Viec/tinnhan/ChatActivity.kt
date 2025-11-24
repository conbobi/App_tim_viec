package com.example.app_tim_viec.UI.Chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_tim_viec.databinding.ActivityChatBinding
import com.example.app_tim_viec.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ChatAdapter
    private val messageList = ArrayList<Message>()

    private var receiverId: String? = null
    private var receiverName: String? = null
    private var chatRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Lấy dữ liệu từ Intent (người mình muốn chat cùng)
        receiverId = intent.getStringExtra("userId")
        receiverName = intent.getStringExtra("userName")
        val myUid = auth.currentUser?.uid

        if (receiverId == null || myUid == null) {
            Toast.makeText(this, "Lỗi xác định người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvTitleName.text = receiverName ?: "Chat"
        binding.btnBack.setOnClickListener { finish() }

        // 2. Tạo RoomID DUY NHẤT (không trùng) bằng cách sắp xếp ID
        chatRoomId = if (myUid < receiverId!!) {
            "${myUid}_${receiverId}"
        } else {
            "${receiverId}_${myUid}"
        }

        // 3. Setup RecyclerView
        adapter = ChatAdapter(messageList)
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = adapter

        // 4. Lắng nghe tin nhắn realtime
        listenMessages()

        // 5. Gửi tin nhắn
        binding.btnSend.setOnClickListener {
            val msgText = binding.edtMessage.text.toString().trim()
            if (msgText.isNotEmpty()) {
                sendMessage(msgText, myUid)
            }
        }
    }

    private fun sendMessage(text: String, senderId: String) {
        val timestamp = System.currentTimeMillis()
        val message = Message(senderId, text, timestamp)

        binding.edtMessage.setText("") // Xóa ô nhập

        // Lưu vào subcollection 'Messages'
        db.collection("Chats").document(chatRoomId!!)
            .collection("Messages")
            .add(message)
            .addOnFailureListener {
                Toast.makeText(this, "Gửi lỗi", Toast.LENGTH_SHORT).show()
            }

        // (Tùy chọn) Cập nhật thông tin phòng chat bên ngoài để làm danh sách chat sau này
        val roomInfo = mapOf(
            "lastMessage" to text,
            "timestamp" to timestamp,
            "participants" to listOf(senderId, receiverId)
        )
        db.collection("Chats").document(chatRoomId!!).set(roomInfo)
    }

    private fun listenMessages() {
        db.collection("Chats").document(chatRoomId!!)
            .collection("Messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    messageList.clear()
                    for (doc in value) {
                        val msg = doc.toObject(Message::class.java)
                        messageList.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    // Cuộn xuống cuối
                    if (messageList.isNotEmpty()) {
                        binding.recyclerChat.scrollToPosition(messageList.size - 1)
                    }
                }
            }
    }
}