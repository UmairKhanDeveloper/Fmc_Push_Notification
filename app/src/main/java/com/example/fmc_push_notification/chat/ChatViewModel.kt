package com.example.fmc_push_notification.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID




class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val db = Firebase.database
    private var messageListener: ValueEventListener? = null
    private var currentChannelId: String? = null

    fun listenForChannelMessages(channelId: String) {
        if (currentChannelId == channelId) return

        messageListener?.let {
            db.getReference("channelMessages").child(currentChannelId!!).removeEventListener(it)
        }

        currentChannelId = channelId
        val messagesRef = db.getReference("channelMessages").child(channelId)

        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                snapshot.children.forEach { data ->
                    val message = data.getValue(Message::class.java)
                    message?.let { list.add(it) }
                }
                _messages.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to get messages", error.toException())
            }
        }
        messagesRef.orderByChild("createdAt").addValueEventListener(messageListener!!)
    }

    fun sendMessageToChannel(channelId: String, messageText: String) {
        val currentUser = Firebase.auth.currentUser ?: return
        val messagesRef = db.getReference("channelMessages").child(channelId)
        val messageId = messagesRef.push().key ?: UUID.randomUUID().toString()

        val message = Message(
            id = messageId,
            senderId = currentUser.uid,
            message = messageText,
            createdAt = System.currentTimeMillis(),
            senderName = currentUser.displayName ?: "Anonymous"
        )

        messagesRef.child(messageId).setValue(message)
    }

    override fun onCleared() {
        super.onCleared()
        messageListener?.let {
            currentChannelId?.let { id ->
                db.getReference("channelMessages").child(id).removeEventListener(it)
            }
        }
    }
}