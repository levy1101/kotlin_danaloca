package com.levy.danaloca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levy.danaloca.model.ChatMessage
import com.levy.danaloca.repository.ChatRepository
import com.levy.danaloca.utils.Resource

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private var _messages: MutableLiveData<Resource<List<ChatMessage>>>? = null
    val messages: LiveData<Resource<List<ChatMessage>>>?
        get() = _messages

    private var _sendMessageStatus = MutableLiveData<Resource<String>>()
    val sendMessageStatus: LiveData<Resource<String>> = _sendMessageStatus

    fun loadMessages(otherUserId: String) {
        if (_messages == null) {
            _messages = repository.getMessages(otherUserId)
        }
    }

    fun sendMessage(receiverId: String, message: String) {
        _sendMessageStatus = repository.sendMessage(receiverId, message)
    }

    override fun onCleared() {
        super.onCleared()
        _messages = null
    }
}