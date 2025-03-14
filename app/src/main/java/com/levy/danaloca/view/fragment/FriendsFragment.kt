package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.databinding.FragmentFriendsBinding
import com.levy.danaloca.databinding.ItemFriendBinding
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.utils.Status
import com.levy.danaloca.viewmodel.FriendsViewModel

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: FriendsViewModel
    private lateinit var adapter: FriendsAdapter
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = FriendsViewModel()
        
        // Get current user ID from Firebase Auth
        FirebaseAuth.getInstance().currentUser?.let { 
            currentUserId = it.uid
        } ?: run {
            // If not logged in, show error and return to previous screen
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        
        // Only load data if we have a valid user ID
        if (currentUserId.isNotEmpty()) {
            viewModel.loadUsers(currentUserId)
            viewModel.loadFriendRequests(currentUserId)
        }
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(
            currentUserId = currentUserId,
            onAddFriend = { userId ->
                viewModel.sendFriendRequest(currentUserId, userId)
            },
            onCancelRequest = { requestId ->
                viewModel.cancelFriendRequest(requestId, currentUserId)
            },
            onAcceptRequest = { request ->
                viewModel.acceptFriendRequest(request)
            },
            onDeclineRequest = { requestId ->
                viewModel.cancelFriendRequest(requestId, currentUserId)
            },
            onFriendOptionsSelected = { userId, view ->
                showFriendOptionsMenu(userId, view)
            }
        )
        
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FriendsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner, Observer { result ->
            when (result.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    result.data?.let { adapter.submitUsers(it) }
                }
                Status.ERROR -> showError(result.message ?: "Unknown error")
            }
        })

        viewModel.friendRequests.observe(viewLifecycleOwner, Observer { result ->
            if (result.status == Status.SUCCESS) {
                result.data?.let { adapter.submitFriendRequests(it) }
            }
        })

        viewModel.operationState.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                if (it.status == Status.ERROR) {
                    showError(it.message ?: "Unknown error")
                }
                viewModel.resetOperationState()
            }
        })
    }

    private fun showFriendOptionsMenu(userId: String, anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_friend_options, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_unfollow -> {
                        viewModel.unfollowUser(currentUserId, userId)
                        true
                    }
                    R.id.action_remove -> {
                        viewModel.removeFriend(currentUserId, userId)
                        true
                    }
                    R.id.action_block -> {
                        viewModel.blockUser(currentUserId, userId)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvFriends.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.rvFriends.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvFriends.visibility = View.GONE
        binding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private class FriendsAdapter(
    private val currentUserId: String,
    private val onAddFriend: (String) -> Unit,
    private val onCancelRequest: (String) -> Unit,
    private val onAcceptRequest: (FriendRequest) -> Unit,
    private val onDeclineRequest: (String) -> Unit,
    private val onFriendOptionsSelected: (String, View) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    private var users = emptyList<User>()
    private var friendRequests = emptyList<FriendRequest>()

    fun submitUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun submitFriendRequests(newRequests: List<FriendRequest>) {
        friendRequests = newRequests
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, friendRequests)
    }

    override fun getItemCount(): Int = users.size

    inner class FriendViewHolder(
        private val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, requests: List<FriendRequest>) {
            binding.apply {
                // Set username and profile image
                tvUsername.text = user.username ?: user.fullName
                ivProfile.setImageResource(R.drawable.default_avatar)

                // Set location if available
                tvLocation.text = user.location
                tvLocation.visibility = if (user.location.isNotEmpty()) View.VISIBLE else View.GONE

                // Handle button states based on relationship status
                when {
                    user.isFriendWith(currentUserId) -> {
                        showFriendsOptions()
                        btnFriendOptions.setOnClickListener { 
                            onFriendOptionsSelected(user.id, it)
                        }
                    }
                    requests.any { it.senderId == user.id && it.receiverId == currentUserId } -> {
                        showReceivedRequest()
                        btnAccept.setOnClickListener {
                            requests.find { it.senderId == user.id }?.let {
                                onAcceptRequest(it)
                            }
                        }
                        btnDecline.setOnClickListener {
                            requests.find { it.senderId == user.id }?.let {
                                onDeclineRequest(it.id)
                            }
                        }
                    }
                    requests.any { it.senderId == currentUserId && it.receiverId == user.id } -> {
                        showCancelRequest()
                        btnCancelRequest.setOnClickListener {
                            requests.find { it.receiverId == user.id }?.let {
                                onCancelRequest(it.id)
                            }
                        }
                    }
                    else -> {
                        showAddFriend()
                        btnAddFriend.setOnClickListener {
                            onAddFriend(user.id)
                        }
                    }
                }
            }
        }

        private fun showAddFriend() {
            binding.apply {
                btnAddFriend.visibility = View.VISIBLE
                btnCancelRequest.visibility = View.GONE
                layoutRequestActions.visibility = View.GONE
                btnFriendOptions.visibility = View.GONE
            }
        }

        private fun showCancelRequest() {
            binding.apply {
                btnAddFriend.visibility = View.GONE
                btnCancelRequest.visibility = View.VISIBLE
                layoutRequestActions.visibility = View.GONE
                btnFriendOptions.visibility = View.GONE
            }
        }

        private fun showReceivedRequest() {
            binding.apply {
                btnAddFriend.visibility = View.GONE
                btnCancelRequest.visibility = View.GONE
                layoutRequestActions.visibility = View.VISIBLE
                btnFriendOptions.visibility = View.GONE
            }
        }

        private fun showFriendsOptions() {
            binding.apply {
                btnAddFriend.visibility = View.GONE
                btnCancelRequest.visibility = View.GONE
                layoutRequestActions.visibility = View.GONE
                btnFriendOptions.visibility = View.VISIBLE
            }
        }
    }
}