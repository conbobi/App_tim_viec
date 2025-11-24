package com.example.app_tim_viec.UI.Nguoi_Tim_Viec.tinnhan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_tim_viec.R
import com.example.app_tim_viec.models.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatRoomAdapter(
    private var chatList: List<ChatRoom>,
    private val onItemClick: (String, String) -> Unit // Trả về (OtherUserId, OtherUserName)
) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    private val myUid = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLastMsg: TextView = itemView.findViewById(R.id.tvLastMessage)

        fun bind(room: ChatRoom) {
            tvLastMsg.text = room.lastMessage

            // Tìm ID của người kia (người không phải là mình)
            val otherId = room.participants.find { it != myUid } ?: return

            // Lấy tên người kia từ Firestore (Bảng users)
            // Lưu ý: Để tối ưu, bạn nên lưu tên người kia vào ChatRoom luôn khi chat
            // Nhưng ở đây ta query lại cho đơn giản
            db.collection("users").document(otherId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("hoTen") ?: doc.getString("companyName") ?: "Người dùng"
                    tvName.text = name

                    // Khi bấm vào item -> Mở chat với người này
                    itemView.setOnClickListener {
                        onItemClick(otherId, name)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount() = chatList.size

    fun updateData(newList: List<ChatRoom>) {
        chatList = newList
        notifyDataSetChanged()
    }
}