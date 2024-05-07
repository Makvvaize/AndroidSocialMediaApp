package com.example.socialmedia

data class Message(
    val senderId: String,
    val receiverId: String,
    val senderDisplayName:String?,
    val content: String,
    val timestamp: Long
)
