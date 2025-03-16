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
import com.levy.danaloca.R
import com.levy.danaloca.model.Post
import com.levy.danaloca.viewmodel.UserViewModel
import com.levy.danaloca.viewmodel.HomeViewModel
import com.levy.danaloca.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class PostAdapter(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val userViewModel: UserViewModel,
    private val homeViewModel: HomeViewModel
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts = mutableListOf<Post>()
    private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("EEEE, HH:mm", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
    private val userNameCache = mutableMapOf<String, String>()  // Cache for usernames

    private fun getFormattedTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            // Less than 1 minute
            diff < 60_000 -> "Just now"
            
            // Less than 1 hour
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            
            // Less than 24 hours
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            
            // This week (less than 7 days)
            diff < 604800_000 -> timeFormat.format(Date(timestamp))
            
            // This year
            Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.YEAR) ==
            Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR) ->
                monthDayFormat.format(Date(timestamp))
            
            // Last year
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
    }

    var listener: PostListener? = null

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        userNameCache.clear() // Clear the cache when updating all posts
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
    fun bind(post: Post) {
       Log.d("PostAdapter", "Binding post: ${post.id}, userId: ${post.userId}")

       if (post.userId.isBlank()) {
           userName.text = "Unknown User"
       } else {
           // First check cache
           val cachedName = userNameCache[post.userId]
           if (cachedName != null) {
               userName.text = cachedName
           } else {
               // Keep the current text while loading
               lifecycleScope.launch {
                   try {
                       val fullName = userViewModel.GetUserFullName(post.userId)
                       Log.d("PostAdapter", "Received fullName: $fullName for userId: ${post.userId}")

                       // Cache the username
                       userNameCache[post.userId] = fullName

                       // Only update if the ViewHolder is still bound to the same post
                       val position = adapterPosition
                       if (position != RecyclerView.NO_POSITION &&
                           position < posts.size &&
                           posts[position].id == post.id) {
                           userName.text = fullName
                       }
                   } catch (e: Exception) {
                       if (e is CancellationException) throw e
                       Log.e("PostAdapter", "Error loading user name for userId: ${post.userId}", e)
                   }
               }
           }
       }

       // Set other post details
       timestamp.text = getFormattedTimestamp(post.timestamp)
       content.text = post.content
       likeCount.text = "${post.likes}"
       commentCount.text = "${post.comments}"

       // Handle post image
       if (post.imageBase64.isNotBlank()) {
           ImageUtils.base64ToBitmap(post.imageBase64)?.let { bitmap ->
               postImage.setImageBitmap(bitmap)
               postImage.visibility = View.VISIBLE
           }
       } else {
           postImage.visibility = View.GONE
       }

       // Handle location icon
       locationIcon.visibility = if (post.latitude != null && post.longitude != null) {
           View.VISIBLE
       } else {
           View.GONE
       }

       // Set up click listeners
       likeButton.setOnClickListener {
           listener?.onLikeClicked(post)
       }

       commentButton.setOnClickListener {
           listener?.onCommentClicked(post)
       }

       moreButton.setOnClickListener {
           listener?.onMoreClicked(post)
       }

       // Set up long click listener for posts with location
       if (post.latitude != null && post.longitude != null) {
           itemView.setOnLongClickListener {
               listener?.onPostLongPressed(post)
               true
           }
       } else {
           itemView.setOnLongClickListener(null)
       }

       // Update like icon based on current user's like status
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