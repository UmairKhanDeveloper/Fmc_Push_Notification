package com.example.fmc_push_notification.firebase_realtimedatabase

data class RealTimeUser(
    val items: RealTimeItems = RealTimeItems(),
    val key: String? = ""
) {
    data class RealTimeItems(
        var text: String = "",
        var senderId: String = "" 
    )
}