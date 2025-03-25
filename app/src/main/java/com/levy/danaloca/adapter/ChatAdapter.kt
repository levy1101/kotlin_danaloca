package com.levy.danaloca.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.databinding.ItemChatMessageBinding
import com.levy.danaloca.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    private lateinit var userViewModel: com.levy.danaloca.viewmodel.UserViewModel

    fun setUserViewModel(viewModel: com.levy.danaloca.viewmodel.UserViewModel) {
        userViewModel = viewModel
    }

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            // Load sender avatar for received messages
            if (message.senderId != currentUserId) {
                loadSenderAvatar(message.senderId)
            }

            if (message.senderId == currentUserId) {
                // Show sent message
                binding.sentMessageLayout.visibility = View.VISIBLE
                binding.receivedMessageLayout.visibility = View.GONE
                binding.sentMessageText.text = message.message
                
                // Set timestamp for sent message
                binding.sentTime.text = formatTime(message.timestamp)
            } else {
                // Show received message
                binding.sentMessageLayout.visibility = View.GONE
                binding.receivedMessageLayout.visibility = View.VISIBLE
                binding.receivedMessageText.text = message.message
                
                // Set timestamp for received message
                binding.receivedTime.text = formatTime(message.timestamp)
            }
        }

        private fun loadSenderAvatar(senderId: String) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    val avatarUrl = userViewModel.GetUserAvatar(senderId)
                    com.levy.danaloca.utils.GlideUtils.loadProfileImage(
                        binding.senderAvatar,
                        avatarUrl
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ChatAdapter", "Error loading avatar: ${e.message}")
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            // You can customize this format as needed
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return timeFormat.format(java.util.Date(timestamp))
        }
    }
}