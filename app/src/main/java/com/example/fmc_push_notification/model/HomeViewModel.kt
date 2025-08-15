package com.example.fmc_push_notification.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class HomeViewModel : ViewModel() {

    private val firebaseDatabase = Firebase.database
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        listenForChannels()
    }

    private fun listenForChannels() {
        firebaseDatabase.getReference("channels")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Channel>()
                    snapshot.children.forEach { data ->
                        val channel = data.getValue(Channel::class.java)
                      channel?.let { list.add(it.copy(id = data.key!!)) }
                    }
                    _channels.value = list.sortedByDescending { it.createdAt }
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeViewModel", "Failed to get channels", error.toException())
                }
            })
    }

    fun addChannel(name: String) {
        val channelsRef = firebaseDatabase.getReference("channels")
        val key = channelsRef.push().key ?: return

        val newChannel = Channel(id = key, name = name, createdAt = System.currentTimeMillis())

        channelsRef.child(key).setValue(newChannel)
    }
}