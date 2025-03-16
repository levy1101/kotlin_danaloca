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
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        arguments?.let {
            userId = it.getString("userId")
            userId?.let { id -> viewModel.loadMessages(id) }
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
    }

    private fun setupUI() {
        setupRecyclerView()
        setupMessageInput()
        setupBackButton()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
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
                viewModel.sendMessage(userId!!, message)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupObservers() {
        viewModel.messages?.observe(viewLifecycleOwner) { result ->
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

        viewModel.sendMessageStatus.observe(viewLifecycleOwner) { result ->
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