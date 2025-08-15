package com.example.fmc_push_notification.chat


data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val senderName: String = "",
    val senderImage: String? = null,
    val imageUrl: String? = null
)