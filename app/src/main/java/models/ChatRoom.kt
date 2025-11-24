package com.example.app_tim_viec.models

data class ChatRoom(
    var roomId: String = "",
    var lastMessage: String = "",
    var timestamp: Long = 0,
    var participants: List<String> = listOf()
)