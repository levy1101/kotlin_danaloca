package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.UserRepository
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.utils.Status
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.viewmodel.HomeViewModel

class MyProfileFragment : Fragment(), PostAdapter.PostListener {

    private lateinit var userRepository: UserRepository
    private lateinit var postRepository: PostRepository
    private var currentUserId: String? = null

    // ViewModels
    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel

    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    
    // SwipeRefreshLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // UI Elements
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var tvLocation: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_myprofile, container, false)

        // Initialize ViewModels
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        // Initialize repositories
        userRepository = UserRepository()
        postRepository = PostRepository()

        // Get current user ID from Firebase Auth
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ProfileFragment", "Current user ID: $currentUserId")

        // Initialize UI elements
        initViews(view)

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.posts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel)
        postAdapter.listener = this
        recyclerView.adapter = postAdapter

        // Load user data and posts
        loadUserData()
        loadUserPosts()

        return view
    }

    private fun initViews(view: View) {
        tvFullName = view.findViewById(R.id.tv_full_name)
        tvEmail = view.findViewById(R.id.tv_email)
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number)
        tvGender = view.findViewById(R.id.tv_gender)
        tvAge = view.findViewById(R.id.tv_age)
        tvBirthdate = view.findViewById(R.id.tv_birthdate)
        tvLocation = view.findViewById(R.id.tv_location)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadUserData()
            loadUserPosts()
        }
    }

    private fun loadUserData() {
        if (currentUserId.isNullOrEmpty()) {
            showError("User not logged in")
            swipeRefreshLayout.isRefreshing = false
            return
        }

        Log.d("ProfileFragment", "Loading user data for ID: $currentUserId")
        userRepository.getUser(currentUserId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        displayUserData(it)
                    } ?: showError("User data is null")
                } else {
                    showError("User data not found")
                }
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Error loading user data: ${error.message}")
                Log.e("ProfileFragment", "Database error: ${error.message}")
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun displayUserData(user: User) {
        tvFullName.text = user.fullName
        tvEmail.text = user.email
        tvPhoneNumber.text = user.phoneNumber
        tvGender.text = user.gender
        tvAge.text = user.age
        tvBirthdate.text = user.birthdate
        tvLocation.text = user.location
    }

    private fun loadUserPosts() {
        if (currentUserId.isNullOrEmpty()) {
            showError("User not logged in")
            swipeRefreshLayout.isRefreshing = false
            return
        }

        Log.d("ProfileFragment", "Loading posts for user ID: $currentUserId")
        postRepository.getPosts().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val posts = resource.data
                    if (posts != null) {
                        // Filter posts for current user
                        val userPosts = posts.filter { it.userId == currentUserId }
                        Log.d("ProfileFragment", "Found ${userPosts.size} posts for current user")
                        // Update adapter with filtered posts
                        postAdapter.updatePosts(userPosts)
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
                Status.ERROR -> {
                    showError("Error loading posts: ${resource.message}")
                    swipeRefreshLayout.isRefreshing = false
                }
                Status.LOADING -> {
                    Log.d("ProfileFragment", "Loading posts...")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.e("ProfileFragment", message)
    }

    // PostAdapter.PostListener Interface Implementation
    override fun onLikeClicked(post: Post) {
        currentUserId?.let { userId ->
            homeViewModel.toggleLike(post.id, userId)
        }
    }

    override fun onCommentClicked(post: Post) {
        // Handle comment click if needed
    }

    override fun onMoreClicked(post: Post) {
        // Handle more options click if needed
    }
}