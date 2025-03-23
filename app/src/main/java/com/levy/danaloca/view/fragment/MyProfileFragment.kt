package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.levy.danaloca.R
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.model.Post
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MyProfileFragment : Fragment(), PostAdapter.PostListener {

    private val userViewModel: UserViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Profile info views
    private lateinit var fullNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var genderText: TextView
    private lateinit var ageText: TextView
    private lateinit var birthdateText: TextView
    private lateinit var locationText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_myprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        loadUserData()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.posts_recycler_view)
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout)
        
        // Initialize profile info views
        fullNameText = view.findViewById(R.id.tv_full_name)
        emailText = view.findViewById(R.id.tv_email)
        phoneText = view.findViewById(R.id.tv_phone_number)
        genderText = view.findViewById(R.id.tv_gender)
        ageText = view.findViewById(R.id.tv_age)
        birthdateText = view.findViewById(R.id.tv_birthdate)
        locationText = view.findViewById(R.id.tv_location)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel).apply {
            listener = this@MyProfileFragment
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
        userViewModel.getCurrentUser()?.let { firebaseUser ->
            // Get user details
            userViewModel.getUser(firebaseUser.uid)
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                user?.let { updateProfileInfo(it) }
            }

            // Load user's posts
            homeViewModel.getPosts().observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val userPosts = resource.data?.filter { it.userId == firebaseUser.uid }
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
        }
    }

    private fun updateProfileInfo(user: User) {
        fullNameText.text = user.fullName.ifBlank { "Not set" }
        emailText.text = user.email.ifBlank { "Not set" }
        phoneText.text = user.phoneNumber.ifBlank { "Not set" }
        genderText.text = user.gender.ifBlank { "Not set" }
        ageText.text = user.age.ifBlank { "Not set" }
        birthdateText.text = user.birthdate.ifBlank { "Not set" }
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