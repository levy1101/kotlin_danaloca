package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.levy.danaloca.R
import com.levy.danaloca.adapter.PostAdapter
import com.levy.danaloca.model.Post
import com.levy.danaloca.view.activity.LocationPreviewDialog
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class PostFragment : Fragment(), PostAdapter.PostListener {

    protected val userViewModel: UserViewModel by activityViewModels()
    protected val homeViewModel: HomeViewModel by activityViewModels()
    protected lateinit var postAdapter: PostAdapter
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPostComponents(view)
    }

    protected open fun setupPostComponents(view: View) {
        recyclerView = view.findViewById(getRecyclerViewId())
        swipeRefresh = view.findViewById(getSwipeRefreshId())
        
        setupRecyclerView()
        setupSwipeRefresh()
    }

    abstract fun getRecyclerViewId(): Int
    abstract fun getSwipeRefreshId(): Int

    protected open fun setupRecyclerView() {
        postAdapter = PostAdapter(lifecycleScope, userViewModel, homeViewModel).apply {
            listener = this@PostFragment
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    protected abstract fun setupSwipeRefresh()

    protected fun observePosts(onPostsLoaded: (List<Post>) -> Unit) {
        homeViewModel.getPosts().observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { posts ->
                        onPostsLoaded(posts)
                    }
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

    // PostAdapter.PostListener implementations
    override fun onLikeClicked(post: Post) {
        lifecycleScope.launch {
            userViewModel.getCurrentUser()?.uid?.let { userId ->
                homeViewModel.toggleLike(post.id, userId)
            }
        }
    }

    override fun onCommentClicked(post: Post) {
        showPostDetail(post)
    }

    override fun onMoreClicked(post: Post) {
        // Override in child if needed
    }

    override fun onPostLongPressed(post: Post) {
        if (post.latitude != null && post.longitude != null) {
            showLocationPreview(post.latitude, post.longitude)
        }
    }

    private fun showLocationPreview(latitude: Double, longitude: Double) {
        val dialog = LocationPreviewDialog.newInstance(latitude, longitude)
        dialog.show(childFragmentManager, "location_preview")
    }

    override fun onBookmarkClicked(post: Post) {
        // Override in child if needed
    }

    override fun onPostClicked(post: Post) {
        showPostDetail(post)
    }

    protected fun showPostDetail(post: Post) {
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