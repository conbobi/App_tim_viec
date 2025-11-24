package com.example.app_tim_viec.UI.Chat

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app_tim_viec.R
import com.example.app_tim_viec.models.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(private var messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: LinearLayout = itemView.findViewById(R.id.layoutMessage)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)

        fun bind(msg: Message) {
            tvContent.text = msg.content

            if (msg.senderId == currentUid) {
                // Tin nhắn của MÌNH -> Căn PHẢI, màu XANH
                layout.gravity = Gravity.END
                tvContent.setBackgroundColor(Color.parseColor("#DCF8C6")) // Màu xanh nhạt
            } else {
                // Tin nhắn NGƯỜI KHÁC -> Căn TRÁI, màu XÁM
                layout.gravity = Gravity.START
                tvContent.setBackgroundColor(Color.parseColor("#E0E0E0")) // Màu xám
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    fun updateData(newList: List<Message>) {
        messages = newList
        notifyDataSetChanged()
    }
}