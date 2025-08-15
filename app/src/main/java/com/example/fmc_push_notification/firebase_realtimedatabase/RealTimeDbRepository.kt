package com.example.fmc_push_notification.firebase_realtimedatabase

import android.content.Context
import android.provider.Settings
import com.example.fmc_push_notification.firebase.ResultState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RealTimeDbRepository(
    private val db: DatabaseReference,
    private val context: Context
) : RealTimeRepository {

    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun insert(item: RealTimeUser.RealTimeItems): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)

            // डिवाइस आईडी प्राप्त करें और इसे आइटम में सेट करें
            val deviceId = getDeviceId()
            item.senderId = deviceId

            // 'chats' नामक एक साझा नोड में डेटा भेजें
            val chatNode = db.child("chats")

            chatNode.push().setValue(item)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        trySend(ResultState.Success("Data Inserted Successfully"))
                    }
                }
                .addOnFailureListener {
                    trySend(ResultState.Error(it))
                }

            awaitClose { close() }
        }

    override fun getItems(): Flow<ResultState<List<RealTimeUser>>> = callbackFlow {
        trySend(ResultState.Loading)

        // 'users' के बजाय 'chats' नोड को सुनें
        val chatNode = db.child("chats")

        val valueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<RealTimeUser>()
                snapshot.children.forEach { dataSnapshot ->
                    val item = dataSnapshot.getValue(RealTimeUser.RealTimeItems::class.java)
                    val key = dataSnapshot.key
                    if (item != null && key != null) {
                        itemList.add(RealTimeUser(item, key))
                    }
                }
                trySend(ResultState.Success(itemList))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Error(Exception(error.message)))
            }
        }

        chatNode.addValueEventListener(valueEvent)

        awaitClose {
            chatNode.removeEventListener(valueEvent)
            close()
        }
    }

    // डिलीट और अपडेट फ़ंक्शंस को भी 'chats' नोड का उपयोग करने के लिए अपडेट करें
    override fun delete(key: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val chatNode = db.child("chats")

        chatNode.child(key).removeValue()
            .addOnCompleteListener {
                trySend(ResultState.Success("Item deleted"))
            }
            .addOnFailureListener {
                trySend(ResultState.Error(it))
            }

        awaitClose { close() }
    }

    override fun update(res: RealTimeUser): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val chatNode = db.child("chats")
        val map = mapOf("text" to res.items.text) // "text" फ़ील्ड को अपडेट करें

        chatNode.child(res.key!!).updateChildren(map)
            .addOnCompleteListener {
                trySend(ResultState.Success("Updated successfully"))
            }
            .addOnFailureListener {
                trySend(ResultState.Error(it))
            }

        awaitClose { close() }
    }
}