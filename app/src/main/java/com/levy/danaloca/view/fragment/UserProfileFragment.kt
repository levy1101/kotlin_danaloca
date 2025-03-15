package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.databinding.FragmentUserprofileBinding
import com.levy.danaloca.model.Post
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Status
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.viewmodel.UserProfileViewModel

class UserProfileFragment : Fragment(), PostAdapter.PostListener {
    private var _binding: FragmentUserprofileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserProfileViewModel
    private lateinit var postAdapter: PostAdapter
    private lateinit var homeViewModel: HomeViewModel
    private var currentUserId: String = ""
    private var profileUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = UserProfileViewModel()
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        
        // Get current user ID
        FirebaseAuth.getInstance().currentUser?.let {
            currentUserId = it.uid
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserprofileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupBackButton()
        setupSwipeRefresh()

        // Get userId from arguments and load profile
        arguments?.getString("userId")?.let { userId ->
            profileUserId = userId
            Log.d("UserProfileFragment", "Loading profile for user: $userId")
            
            // Setup friend actions after we have both userIds
            setupFriendActions()
            
            // Load the profile data and posts
            viewModel.loadUserProfile(userId)
        }
    }

    private fun setupRecyclerView() {
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel)
        postAdapter.listener = this
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            profileUserId.let { userId ->
                viewModel.loadUserProfile(userId)
            }
        }
    }

    private fun setupObservers() {
        // Observe user data
        viewModel.user.observe(viewLifecycleOwner, Observer { result ->
            when (result.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> {
                    hideLoading()
                    updateUIState()
                }
                Status.ERROR -> showError(result.message ?: "Unknown error")
            }
        })

        // Observe friend requests
        viewModel.friendRequests.observe(viewLifecycleOwner, Observer { result ->
            when (result.status) {
                Status.LOADING -> { /* Loading state - no action needed */ }
                Status.SUCCESS -> updateUIState()
                Status.ERROR -> Log.e("UserProfileFragment", "Error loading friend requests: ${result.message}")
            }
        })

        // Observe operation state
        viewModel.operationState.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                when (it.status) {
                    Status.SUCCESS -> {
                        // Force UI update after successful operation
                        updateUIState()
                    }
                    Status.ERROR -> {
                        showError(it.message ?: "Unknown error")
                    }
                    else -> {}
                }
                viewModel.resetOperationState()
            }
        })

        // Observe user posts
        viewModel.userPosts.observe(viewLifecycleOwner, Observer { result ->
            when (result.status) {
                Status.LOADING -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    Log.d("UserProfileFragment", "Loading posts...")
                }
                Status.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    result.data?.let { posts ->
                        Log.d("UserProfileFragment", "Received ${posts.size} posts")
                        posts.forEach { post ->
                            Log.d("UserProfileFragment", "Post: id=${post.id}, userId=${post.userId}, content=${post.content}")
                        }
                        postAdapter.updatePosts(posts)
                    } ?: Log.d("UserProfileFragment", "Received null posts data")
                }
                Status.ERROR -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    val error = "Error loading posts: ${result.message}"
                    Log.e("UserProfileFragment", error)
                    showError(error)
                }
            }
        })
    }

    private fun updateUIState() {
        val userData = viewModel.user.value?.data
        val friendRequestsData = viewModel.friendRequests.value?.data

        if (userData != null) {
            // Update basic user information
            updateUI(userData)
            
            // Update friend actions state with both user and requests data
            binding.friendActions.updateState(userData, friendRequestsData ?: emptyList())
        }
    }

    private fun setupFriendActions() {
        binding.friendActions.apply {
            setUserIds(currentUserId, profileUserId)
            setAddFriendListener { viewModel.sendFriendRequest(currentUserId, it) }
            setCancelRequestListener { requestId -> viewModel.cancelFriendRequest(requestId, profileUserId) }
            setAcceptRequestListener { request -> viewModel.acceptFriendRequest(request) }
            setDeclineRequestListener { requestId, userId -> viewModel.declineFriendRequest(requestId, userId) }
            setFriendOptionsListener { userId, view ->
                showFriendOptionsMenu(userId, view)
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun updateUI(user: User) {
        binding.apply {
            // Set user image (using placeholder for now)
            userProfileImage.setImageResource(R.drawable.default_profile)
            
            // Set user info
            tvUserFullName.text = user.fullName
            tvUserGender.text = user.gender
            tvUserAge.text = user.age
            tvUserLocation.text = user.location.ifEmpty { "Not specified" }
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
        binding.apply {
            infoCard.visibility = View.GONE
            // You might want to add a ProgressBar in your layout
        }
    }

    private fun hideLoading() {
        binding.apply {
            infoCard.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        // You might want to show an error message to the user
        binding.infoCard.visibility = View.GONE
        Log.e("UserProfileFragment", "Error loading profile: $message")
    }

    // PostAdapter.PostListener Implementation
    override fun onLikeClicked(post: Post) {
        currentUserId?.let { userId ->
            homeViewModel.toggleLike(post.id, userId)
        }
    }

    override fun onCommentClicked(post: Post) {
        // Handle comment click if needed
    }

    override fun onMoreClicked(post: Post) {
        // Show options menu if post belongs to current user
        if (post.userId == currentUserId) {
            // Implement post options menu (edit, delete, etc.)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}