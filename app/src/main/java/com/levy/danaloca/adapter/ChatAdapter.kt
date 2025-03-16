package com.levy.danaloca.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.databinding.ItemChatMessageBinding
import com.levy.danaloca.model.ChatMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private val messages = mutableListOf<ChatMessage>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.senderId == currentUserId) {
                // Show sent message
                binding.sentMessageLayout.visibility = View.VISIBLE
                binding.receivedMessageLayout.visibility = View.GONE
                binding.sentMessageText.text = message.message
            } else {
                // Show received message
                binding.sentMessageLayout.visibility = View.GONE
                binding.receivedMessageLayout.visibility = View.VISIBLE
                binding.receivedMessageText.text = message.message
            }
        }
    }
}