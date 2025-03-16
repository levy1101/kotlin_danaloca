package com.levy.danaloca.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.levy.danaloca.R
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.Status
import com.levy.danaloca.view.SearchActivity
import com.levy.danaloca.view.CreatePostActivity
import com.levy.danaloca.view.MessagesActivity
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel

class HomeFragment : Fragment(), ActionBarFragment.ActionBarListener, PostAdapter.PostListener {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var actionBarFragment: ActionBarFragment
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var postAdapter: PostAdapter
    private val viewModel: HomeViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        
        setupActionBar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupActionBar() {
        actionBarFragment = childFragmentManager.findFragmentById(R.id.actionBarFragment) as ActionBarFragment
        actionBarFragment.setActionBarListener(this)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            lifecycleScope,
            userViewModel,
            viewModel
        ).apply {
            listener = this@HomeFragment
        }
        postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
    }

    private fun observeViewModel() {
        viewModel.getPosts().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    swipeRefresh.isRefreshing = false
                    resource.data?.let { posts ->
                        postAdapter.updatePosts(posts)
                    }
                }
                Status.ERROR -> {
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getLikeUpdateStatus().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.ERROR -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {} // Loading and Success states are handled by the posts observer
            }
        }

        viewModel.getDeleteStatus().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.ERROR -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {} // Loading and Success states are handled by the posts observer
            }
        }
    }

    // ActionBarListener implementations
    override fun onSearchClicked() {
        startActivity(Intent(activity, SearchActivity::class.java))
    }

    override fun onAddClicked() {
        startActivity(Intent(activity, CreatePostActivity::class.java))
    }


    // PostListener implementations
    override fun onLikeClicked(post: Post) {
        userViewModel.getCurrentUser()?.uid?.let { userId ->
            viewModel.toggleLike(post.id, userId)
        } ?: run {
            Toast.makeText(context, "Please sign in to like posts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePostLikeStatus(post: Post, isLiked: Boolean) {
        postAdapter.notifyItemChanged(postAdapter.getPostPosition(post))
    }

    override fun onCommentClicked(post: Post) {
        // TODO: Navigate to comments screen
        Toast.makeText(context, "Comments feature coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onMoreClicked(post: Post) {
        // TODO: Show bottom sheet with options
        Toast.makeText(context, "More options coming soon", Toast.LENGTH_SHORT).show()
    }
}