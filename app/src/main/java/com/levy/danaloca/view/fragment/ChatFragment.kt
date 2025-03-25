package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.levy.danaloca.adapter.ChatAdapter
import com.levy.danaloca.databinding.FragmentChatBinding
import com.levy.danaloca.utils.Status
import com.levy.danaloca.viewmodel.ChatViewModel

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: com.levy.danaloca.viewmodel.UserViewModel
    private lateinit var chatAdapter: ChatAdapter
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(this)[com.levy.danaloca.viewmodel.UserViewModel::class.java]
        arguments?.let {
            userId = it.getString("userId")
            userId?.let { id ->
                chatViewModel.loadMessages(id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        userId?.let { id -> loadUserDetails(id) }
    }

    private fun setupUI() {
        setupRecyclerView()
        setupMessageInput()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        chatAdapter.setUserViewModel(userViewModel)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupMessageInput() {
        binding.btnSend.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty() && userId != null) {
                chatViewModel.sendMessage(userId!!, message)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadUserDetails(userId: String) {
        userViewModel.getUser(userId)
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvChatTitle.text = it.fullName
                com.levy.danaloca.utils.GlideUtils.loadProfileImage(
                    binding.profileImage,
                    it.avatarUrl
                )
            }
        }
    }

    private fun setupObservers() {
        chatViewModel.messages?.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    result.data?.let { messages ->
                        chatAdapter.updateMessages(messages)
                        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showLoading()
                }
            }
        }

        chatViewModel.sendMessageStatus.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.ERROR -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                else -> {} // Other states don't need UI updates
            }
        }
    }

    private fun showLoading() {
        // Add loading indicator if needed
    }

    private fun hideLoading() {
        // Hide loading indicator if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}