package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.model.Post
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.view.custom.FriendActionsView
import com.levy.danaloca.viewmodel.FriendsViewModel
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel

class UserProfileFragment : Fragment(), PostAdapter.PostListener {

    private val userViewModel: UserViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Profile info views
    private lateinit var profileTitle: TextView
    private lateinit var fullNameText: TextView
    private lateinit var genderText: TextView
    private lateinit var ageText: TextView
    private lateinit var locationText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var messageButton: ImageButton
    private lateinit var friendActions: FriendActionsView

    private var currentUserId: String? = null
    private var userId: String? = null

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): UserProfileFragment {
            return UserProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        userId = arguments?.getString(ARG_USER_ID)
        
        if (userId == null) {
            parentFragmentManager.popBackStack()
            return
        }

        initViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFriendActions()
        setupMessageButton()
        loadUserData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.posts_recycler_view)
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout)
        
        // Initialize profile info views
        profileTitle = view.findViewById(R.id.profile_title)
        fullNameText = view.findViewById(R.id.tv_user_full_name)
        genderText = view.findViewById(R.id.tv_user_gender)
        ageText = view.findViewById(R.id.tv_user_age)
        locationText = view.findViewById(R.id.tv_user_location)
        backButton = view.findViewById(R.id.btn_back)
        messageButton = view.findViewById(R.id.btn_message)
        friendActions = view.findViewById(R.id.friend_actions)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupMessageButton() {
        messageButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            findNavController().navigate(
                R.id.action_userProfileFragment_to_chatFragment,
                bundle
            )
        }
    }

    private fun setupFriendActions() {
        currentUserId?.let { currentId ->
            userId?.let { profileId ->
                friendActions.apply {
                    setUserIds(currentId, profileId)
                    setAddFriendListener { friendsViewModel.sendFriendRequest(currentId, it) }
                    setCancelRequestListener { requestId -> friendsViewModel.cancelFriendRequest(requestId, profileId) }
                    setAcceptRequestListener { request -> friendsViewModel.acceptFriendRequest(request) }
                    setDeclineRequestListener { requestId, userId -> friendsViewModel.declineFriendRequest(requestId, userId) }
                    setFriendOptionsListener { userId, view ->
                        showFriendOptionsMenu(userId, view)
                    }
                }
            }
        }
    }

    private fun showFriendOptionsMenu(userId: String, anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_friend_options, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_unfollow -> {
                        friendsViewModel.unfollowUser(currentUserId!!, userId)
                        true
                    }
                    R.id.action_remove -> {
                        friendsViewModel.removeFriend(currentUserId!!, userId)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel).apply {
            listener = this@UserProfileFragment
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadUserData()
        }
    }

    private fun loadUserData() {
        userId?.let { id ->
            // Get user details
            userViewModel.getUser(id)
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                user?.let { updateProfileInfo(it) }
            }

            // Load friend requests to update UI state
            currentUserId?.let { currentId ->
                friendsViewModel.loadFriendRequests(currentId)
                friendsViewModel.friendRequests.observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            userViewModel.user.value?.let { user ->
                                friendActions.updateState(user, resource.data)
                            }
                        }
                        is Resource.Loading -> {
                            // Handle loading state if needed
                        }
                        is Resource.Error -> {
                            // Handle error state if needed
                        }
                    }
                }
            }

            // Load user's posts
            homeViewModel.getPosts().observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val userPosts = resource.data?.filter { it.userId == id }
                        postAdapter.updatePosts(userPosts ?: emptyList())
                        swipeRefresh.isRefreshing = false
                    }
                    is Resource.Loading -> {
                        swipeRefresh.isRefreshing = true
                    }
                    is Resource.Error -> {
                        swipeRefresh.isRefreshing = false
                    }
                }
            }

            // Observe friend operations state
            friendsViewModel.operationState.observe(viewLifecycleOwner) { result ->
                result?.let {
                    when (it) {
                        is Resource.Success -> {
                            // Refresh friend requests after successful operation
                            currentUserId?.let { currentId ->
                                friendsViewModel.loadFriendRequests(currentId)
                            }
                        }
                        is Resource.Error -> {
                            // Handle error if needed
                        }
                        is Resource.Loading -> {
                            // Handle loading state if needed
                        }
                    }
                    friendsViewModel.resetOperationState()
                }
            }
        }
    }

    private fun updateProfileInfo(user: User) {
        profileTitle.text = user.fullName
        fullNameText.text = user.fullName.ifBlank { "Not set" }
        genderText.text = user.gender.ifBlank { "Not set" }
        ageText.text = user.age.ifBlank { "Not set" }
        locationText.text = user.location.ifBlank { "Not set" }
    }

    // PostAdapter.PostListener implementations
    override fun onLikeClicked(post: Post) {
        lifecycleScope.launchWhenStarted {
            userViewModel.getCurrentUser()?.uid?.let { userId ->
                homeViewModel.toggleLike(post.id, userId)
            }
        }
    }

    override fun onCommentClicked(post: Post) {
        showPostDetail(post)
    }

    override fun onMoreClicked(post: Post) {
        // Handle more options
    }

    override fun onPostLongPressed(post: Post) {
        // Handle long press
    }

    override fun onBookmarkClicked(post: Post) {
        // Handle bookmark
    }

    override fun onPostClicked(post: Post) {
        showPostDetail(post)
    }

    private fun showPostDetail(post: Post) {
        val detailFragment = PostDetailFragment.newInstance(post.id)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.slide_in_right,
                R.animator.slide_out_left,
                R.animator.slide_in_left,
                R.animator.slide_out_right
            )
            .replace(R.id.nav_host_fragment, detailFragment)
            .addToBackStack(null)
            .commit()
    }
}