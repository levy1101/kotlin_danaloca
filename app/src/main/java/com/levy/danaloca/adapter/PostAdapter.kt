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
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun getPostPosition(post: Post): Int {
        return posts.indexOfFirst { it.id == post.id }
    }

    interface PostListener {
        fun onLikeClicked(post: Post)
        fun onCommentClicked(post: Post)
        fun onMoreClicked(post: Post)
    }

    var listener: PostListener? = null

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
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

        fun bind(post: Post) {
           userName.text = "Loading..." // Show loading state
           
           Log.d("PostAdapter", "Binding post: ${post.id}, userId: ${post.userId}")
           
           if (post.userId.isBlank()) {
               Log.d("PostAdapter", "UserId is blank, showing Unknown User")
               userName.text = "Unknown User"
           } else {
               lifecycleScope.launch {
                   try {
                       Log.d("PostAdapter", "Fetching user name for userId: ${post.userId}")
                       val fullName = userViewModel.GetUserFullName(post.userId)
                       Log.d("PostAdapter", "Received fullName: $fullName for userId: ${post.userId}")
                       
                       // Only update if the ViewHolder is still bound to the same post
                       val position = adapterPosition
                       if (position != RecyclerView.NO_POSITION &&
                           position < posts.size &&
                           posts[position].id == post.id) {
                           userName.text = fullName
                       } else {
                           Log.d("PostAdapter", "ViewHolder no longer bound to same post. Skipping update")
                       }
                   } catch (e: Exception) {
                       if (e is CancellationException) throw e
                       Log.e("PostAdapter", "Error loading user name for userId: ${post.userId}", e)
                       
                       // Only update if the ViewHolder is still bound to the same post
                       val position = adapterPosition
                       if (position != RecyclerView.NO_POSITION &&
                           position < posts.size &&
                           posts[position].id == post.id) {
                           userName.text = "Unknown User"
                       }
                   }
               }
           }
           
           // Set other post details
           timestamp.text = dateFormat.format(Date(post.timestamp))
           content.text = post.content
           likeCount.text = "${post.likes}"
           commentCount.text = "${post.comments}"

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