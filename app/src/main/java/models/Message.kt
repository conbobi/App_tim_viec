package com.example.app_tim_viec.models

data class Message(
    var senderId: String = "",
    var content: String = "",
    var timestamp: Long = 0
)