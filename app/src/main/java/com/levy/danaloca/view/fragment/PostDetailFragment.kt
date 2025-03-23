package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.levy.danaloca.R
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.ImageUtils
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.view.activity.LocationPreviewDialog
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PostDetailFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm", Locale.getDefault())

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarUserName: TextView
    private lateinit var userName: TextView
    private lateinit var timestamp: TextView
    private lateinit var content: TextView
    private lateinit var postImage: ImageView
    private lateinit var locationIcon: ImageView
    private lateinit var locationText: TextView
    private lateinit var locationContainer: View
    private lateinit var likeCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var likeIcon: ImageView

    private var currentPost: Post? = null

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: String): PostDetailFragment {
            return PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        loadPost()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        toolbarUserName = view.findViewById(R.id.toolbarUserName)
        userName = view.findViewById(R.id.userName)
        timestamp = view.findViewById(R.id.timestamp)
        content = view.findViewById(R.id.content)
        postImage = view.findViewById(R.id.postImage)
        locationIcon = view.findViewById(R.id.locationIcon)
        locationText = view.findViewById(R.id.locationText)
        locationContainer = view.findViewById(R.id.locationContainer)
        likeCount = view.findViewById(R.id.likeCount)
        commentCount = view.findViewById(R.id.commentCount)
        likeIcon = view.findViewById(R.id.likeIcon)
    }

    private fun setupListeners() {
        view?.findViewById<View>(R.id.backButton)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view?.findViewById<View>(R.id.likeButton)?.setOnClickListener {
            val post = currentPost
            if (post != null) {
                lifecycleScope.launch {
                    userViewModel.getCurrentUser()?.uid?.let { userId ->
                        homeViewModel.toggleLike(post.id, userId)
                        updateLikeStatus(post)
                    }
                }
            }
        }

        locationContainer.setOnClickListener {
            val post = currentPost
            if (post?.latitude != null && post.longitude != null) {
                showLocationPreview(post.latitude, post.longitude)
            }
        }
    }

    private fun showLocationPreview(latitude: Double, longitude: Double) {
        val dialog = LocationPreviewDialog.newInstance(latitude, longitude)
        dialog.show(childFragmentManager, "location_preview")
    }

    private fun loadPost() {
        val postId = arguments?.getString(ARG_POST_ID)
        if (postId != null) {
            homeViewModel.getPosts().observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.find { it.id == postId }?.let { post ->
                            currentPost = post
                            displayPost(post)
                        }
                    }
                    is Resource.Loading -> {
                        // Show loading state
                    }
                    is Resource.Error -> {
                        // Show error state
                    }
                }
            }
        }
    }

    private fun displayPost(post: Post) {
        // Set user name in toolbar and post
        lifecycleScope.launch {
            try {
                val fullName = userViewModel.GetUserFullName(post.userId)
                toolbarUserName.text = fullName
                userName.text = fullName
            } catch (e: Exception) {
                toolbarUserName.text = "Unknown User"
                userName.text = "Unknown User"
            }
        }

        // Set timestamp with complete date and time
        timestamp.text = dateFormat.format(Date(post.timestamp))

        // Set content
        content.text = post.content

        // Handle post image
        if (post.imageBase64.isNotBlank()) {
            ImageUtils.base64ToBitmap(post.imageBase64)?.let { bitmap ->
                postImage.setImageBitmap(bitmap)
                postImage.visibility = View.VISIBLE
            }
        } else {
            postImage.visibility = View.GONE
        }

        // Handle location
        if (post.latitude != null && post.longitude != null) {
            locationContainer.visibility = View.VISIBLE
            locationText.text = String.format("%.6f, %.6f", post.latitude, post.longitude)
            locationContainer.setOnClickListener {
                showLocationPreview(post.latitude, post.longitude)
            }
        } else {
            locationContainer.visibility = View.GONE
            locationContainer.setOnClickListener(null)
        }

        // Set counts
        likeCount.text = "${post.likes}"
        commentCount.text = "${post.comments}"

        // Update like status
        updateLikeStatus(post)
    }

    private fun updateLikeStatus(post: Post) {
        lifecycleScope.launch {
            try {
                userViewModel.getCurrentUser()?.uid?.let { currentUserId ->
                    val isLiked = homeViewModel.isPostLikedByUser(post, currentUserId)
                    likeIcon.setColorFilter(
                        requireContext().getColor(
                            if (isLiked) R.color.primaryColor else R.color.gray_800
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}