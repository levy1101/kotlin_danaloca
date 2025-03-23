package com.levy.danaloca.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.levy.danaloca.R
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.view.activity.CreatePostActivity
import com.levy.danaloca.view.activity.LocationPreviewDialog
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel

class HomeFragment : Fragment(), PostAdapter.PostListener, ActionBarFragment.ActionBarListener {

    private val userViewModel: UserViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.postsRecyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        
        // Set up ActionBarFragment listener
        val actionBarFragment = childFragmentManager.findFragmentById(R.id.actionBarFragment) as? ActionBarFragment
        actionBarFragment?.setActionBarListener(this)
        
        setupRecyclerView()
        setupSwipeRefresh()

        homeViewModel.getPosts().observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { posts ->
                        postAdapter.updatePosts(posts)
                    }
                    swipeRefresh.isRefreshing = false
                }
                is Resource.Loading -> {
                    swipeRefresh.isRefreshing = true
                }
                is Resource.Error -> {
                    swipeRefresh.isRefreshing = false
                    // Handle error
                }
            }
        }
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel).apply {
            listener = this@HomeFragment
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            homeViewModel.refreshPosts()
        }
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

    // ActionBarFragment.ActionBarListener implementation
    override fun onSearchClicked() {

    }

    override fun onAddClicked() {
        startActivity(Intent(requireContext(), CreatePostActivity::class.java))
    }

    private fun showLocationPreview(latitude: Double, longitude: Double) {
        val dialog = LocationPreviewDialog.newInstance(latitude, longitude)
        dialog.show(childFragmentManager, "location_preview")
    }

    override fun onPostLongPressed(post: Post) {
        if (post.latitude != null && post.longitude != null) {
            showLocationPreview(post.latitude, post.longitude)
        }
    }

    override fun onBookmarkClicked(post: Post) {
        // Bookmark functionality not implemented yet
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