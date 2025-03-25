package com.levy.danaloca.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.levy.danaloca.utils.GlideUtils
import com.levy.danaloca.R
import com.levy.danaloca.model.Post
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class PostAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val userViewModel: UserViewModel,
    private val homeViewModel: HomeViewModel
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts = mutableListOf<Post>()
    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("EEEE, HH:mm", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
    private val userNameCache = mutableMapOf<String, String>()
    private val userAvatarCache = mutableMapOf<String, String>()

    private fun getFormattedTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> timeFormat.format(Date(timestamp))
            Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.YEAR) ==
            Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR) ->
                monthDayFormat.format(Date(timestamp))
            else -> dateFormat.format(Date(timestamp))
        }
    }

    fun getPostPosition(post: Post): Int {
        return posts.indexOfFirst { it.id == post.id }
    }

    interface PostListener {
        fun onLikeClicked(post: Post)
        fun onCommentClicked(post: Post)
        fun onMoreClicked(post: Post)
        fun onPostLongPressed(post: Post)
        fun onBookmarkClicked(post: Post)
        fun onPostClicked(post: Post)
    }

    var listener: PostListener? = null

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        userNameCache.clear()
        userAvatarCache.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val content: TextView = itemView.findViewById(R.id.content)
        private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        private val commentCount: TextView = itemView.findViewById(R.id.commentCount)
        private val likeButton: View = itemView.findViewById(R.id.likeButton)
        private val commentButton: View = itemView.findViewById(R.id.commentButton)
        private val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)
        private val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        private val postImage: ImageView = itemView.findViewById(R.id.postImage)
        private val locationIcon: ImageView = itemView.findViewById(R.id.locationIcon)
        private val bookmarkButton: View = itemView.findViewById(R.id.bookmarkButton)
        
        fun bind(post: Post) {
            itemView.setOnClickListener {
                listener?.onPostClicked(post)
            }

            loadUserDetails(post)
            setupContent(post)
            setupPostImage(post)
            setupLocation(post)
            setupClickListeners(post)
            updateLikeStatus(post)
        }

        private fun loadUserDetails(post: Post) {
            if (post.userId.isNotBlank()) {
                val cachedName = userNameCache[post.userId]
                if (cachedName != null) {
                    userName.text = cachedName
                } else {
                    lifecycleScope.launch {
                        try {
                            val fullName = userViewModel.GetUserFullName(post.userId)
                            userNameCache[post.userId] = fullName
                            if (adapterPosition != RecyclerView.NO_POSITION &&
                                posts[adapterPosition].id == post.id) {
                                userName.text = fullName
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Log.e("PostAdapter", "Error loading user name", e)
                        }
                    }
                }

                val cachedAvatar = userAvatarCache[post.userId]
                if (cachedAvatar != null) {
                    loadUserAvatar(cachedAvatar)
                } else {
                    lifecycleScope.launch {
                        try {
                            val avatarUrl = userViewModel.GetUserAvatar(post.userId)
                            userAvatarCache[post.userId] = avatarUrl
                            if (adapterPosition != RecyclerView.NO_POSITION &&
                                posts[adapterPosition].id == post.id) {
                                loadUserAvatar(avatarUrl)
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Log.e("PostAdapter", "Error loading avatar", e)
                        }
                    }
                }
            }
        }

        private fun setupContent(post: Post) {
            timestamp.text = getFormattedTimestamp(post.timestamp)
            content.text = post.content
            likeCount.text = "${post.likes}"
            commentCount.text = "${post.comments}"
        }

        private fun setupPostImage(post: Post) {
            postImage.visibility = if (post.imageUrl.isNotBlank()) {
                GlideUtils.loadPostImage(postImage, post.imageUrl)
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        private fun setupLocation(post: Post) {
            locationIcon.visibility = if (post.latitude != null && post.longitude != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        private fun loadUserAvatar(avatarUrl: String) {
            GlideUtils.loadProfileImage(userAvatar, avatarUrl)
        }

        private fun setupClickListeners(post: Post) {
            likeButton.setOnClickListener {
                listener?.onLikeClicked(post)
            }

            bookmarkButton.setOnClickListener {
                listener?.onBookmarkClicked(post)
            }

            commentButton.setOnClickListener {
                listener?.onCommentClicked(post)
            }

            moreButton.setOnClickListener {
                listener?.onMoreClicked(post)
            }

            if (post.latitude != null && post.longitude != null) {
                itemView.setOnLongClickListener {
                    listener?.onPostLongPressed(post)
                    true
                }
            } else {
                itemView.setOnLongClickListener(null)
            }
        }

        private fun updateLikeStatus(post: Post) {
            lifecycleScope.launch {
                try {
                    userViewModel.getCurrentUser()?.uid?.let { currentUserId ->
                        val isLiked = homeViewModel.isPostLikedByUser(post, currentUserId)
                        likeIcon.setColorFilter(
                            itemView.context.getColor(
                                if (isLiked) R.color.primaryColor else R.color.gray_800
                            )
                        )
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("PostAdapter", "Error checking like status", e)
                }
            }
        }
    }
}