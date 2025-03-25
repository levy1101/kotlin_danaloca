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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.databinding.FragmentFriendsBinding
import com.levy.danaloca.databinding.ItemFriendBinding
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.User
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
        
        FirebaseAuth.getInstance().currentUser?.let { 
            currentUserId = it.uid
        } ?: run {
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
            onDeclineRequest = { requestId, userId ->
                viewModel.declineFriendRequest(requestId, userId)
            },
            onFriendOptionsSelected = { userId, view ->
                showFriendOptionsMenu(userId, view)
            },
            onItemClick = { userId ->
                val userProfileFragment = UserProfileFragment.newInstance(userId)
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.animator.slide_in_right,
                        R.animator.slide_out_left,
                        R.animator.slide_in_left,
                        R.animator.slide_out_right
                    )
                    .replace(R.id.nav_host_fragment, userProfileFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        
        binding.rvFriends.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FriendsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    result.data?.let { adapter.submitUsers(it) }
                }
                Status.ERROR -> showError(result.message ?: "Unknown error")
            }
        }

        viewModel.friendRequests.observe(viewLifecycleOwner) { result ->
            if (result.status == Status.SUCCESS) {
                result.data?.let { adapter.submitFriendRequests(it) }
            }
        }

        viewModel.operationState.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.status == Status.ERROR) {
                    showError(it.message ?: "Unknown error")
                }
                viewModel.resetOperationState()
            }
        }
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
    private val onDeclineRequest: (String, String) -> Unit,
    private val onFriendOptionsSelected: (String, View) -> Unit,
    private val onItemClick: (String) -> Unit
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
                tvUsername.text = user.fullName
                
                // Handle avatar using Glide
                if (user.avatarUrl.isNotBlank()) {
                    Glide.with(root.context)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(ivProfile)
                } else {
                    ivProfile.setImageResource(R.drawable.default_avatar)
                }

                tvLocation.text = user.location
                tvLocation.visibility = if (user.location.isNotEmpty()) View.VISIBLE else View.GONE

                root.setOnClickListener {
                    onItemClick(user.id)
                }

                friendActions.setUserIds(currentUserId, user.id)
                friendActions.setAddFriendListener { onAddFriend(it) }
                friendActions.setCancelRequestListener { onCancelRequest(it) }
                friendActions.setAcceptRequestListener { onAcceptRequest(it) }
                friendActions.setDeclineRequestListener { requestId, userId -> onDeclineRequest(requestId, userId) }
                friendActions.setFriendOptionsListener { userId, view -> onFriendOptionsSelected(userId, view) }
                friendActions.updateState(user, requests)
            }
        }
    }
}