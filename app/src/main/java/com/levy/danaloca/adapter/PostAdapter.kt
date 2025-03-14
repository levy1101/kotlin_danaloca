package com.levy.danaloca.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.levy.danaloca.R
import com.levy.danaloca.model.Post
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts = mutableListOf<Post>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

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
            userName.text = "User ${post.userId.take(5)}" // Tạm thời hiển thị 5 ký tự đầu của userId
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

            // Update like icon (demonstration purpose)
            likeIcon.setColorFilter(
                itemView.context.getColor(
                    if (post.likes > 0) R.color.primaryColor else R.color.gray_800
                )
            )
        }
    }
}