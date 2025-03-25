package com.levy.danaloca.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.levy.danaloca.R
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.view.activity.CreatePostActivity

class HomeFragment : PostFragment(), ActionBarFragment.ActionBarListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up ActionBarFragment listener
        val actionBarFragment = childFragmentManager.findFragmentById(R.id.actionBarFragment) as? ActionBarFragment
        actionBarFragment?.setActionBarListener(this)

        observePosts()
    }

    override fun getRecyclerViewId() = R.id.postsRecyclerView

    override fun getSwipeRefreshId() = R.id.swipeRefresh

    override fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            homeViewModel.refreshPosts()
        }
    }

    private fun observePosts() {
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

    // ActionBarFragment.ActionBarListener implementation
    override fun onSearchClicked() {
        // Implement search functionality
    }

    override fun onAddClicked() {
        startActivity(Intent(requireContext(), CreatePostActivity::class.java))
    }

    override fun onBookmarkClicked(post: Post) {
        // Bookmark functionality not implemented yet
    }
}