package com.levy.danaloca.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.levy.danaloca.model.ChatMessage
import com.levy.danaloca.utils.Resource

class ChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val messagesRef = database.getReference("messages")

    fun sendMessage(receiverId: String, message: String): MutableLiveData<Resource<String>> {
        val result = MutableLiveData<Resource<String>>()
        result.value = Resource.loading(null)

        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                result.value = Resource.error("User not authenticated", null)
                return result
            }

            val messageId = messagesRef.push().key
            if (messageId == null) {
                result.value = Resource.error("Failed to create message key", null)
                return result
            }

            val chatMessage = ChatMessage(
                id = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                message = message
            )

            messagesRef.child(messageId).setValue(chatMessage)
                .addOnSuccessListener {
                    result.value = Resource.success(messageId)
                }
                .addOnFailureListener { e ->
                    result.value = Resource.error(e.message ?: "Failed to send message", null)
                }
        } catch (e: Exception) {
            result.value = Resource.error(e.message ?: "Failed to send message", null)
        }

        return result
    }

    fun getMessages(otherUserId: String): MutableLiveData<Resource<List<ChatMessage>>> {
        val result = MutableLiveData<Resource<List<ChatMessage>>>()
        result.value = Resource.loading(null)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            result.value = Resource.error("User not authenticated", null)
            return result
        }

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        if ((it.senderId == currentUserId && it.receiverId == otherUserId) ||
                            (it.senderId == otherUserId && it.receiverId == currentUserId)
                        ) {
                            messages.add(it)
                        }
                    }
                }
                messages.sortBy { it.timestamp }
                Log.d("ChatRepository", "Loaded ${messages.size} messages")
                result.value = Resource.success(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Error loading messages: ${error.message}")
                result.value = Resource.error(error.message, null)
            }
        })

        return result
    }
}