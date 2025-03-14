package com.levy.danaloca.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.Resource

class PostRepository {
    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.reference.child("posts")

    fun getPosts(): MutableLiveData<Resource<List<Post>>> {
        val result = MutableLiveData<Resource<List<Post>>>()
        
        result.value = Resource.loading(null)
        
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    postSnapshot.getValue(Post::class.java)?.let { post ->
                        posts.add(post)
                    }
                }
                posts.sortByDescending { it.timestamp }
                result.value = Resource.success(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = Resource.error(error.message, null)
            }
        })

        return result
    }

    fun createPost(post: Post): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        
        result.value = Resource.loading(null)

        postsRef.child(post.id).setValue(post)
            .addOnSuccessListener {
                result.value = Resource.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Resource.error(e.message ?: "Error creating post", null)
            }

        return result
    }

    fun toggleLike(postId: String, userId: String): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        result.value = Resource.loading(null)

        val postRef = postsRef.child(postId)
        
        postRef.get().addOnSuccessListener { snapshot ->
            val post = snapshot.getValue(Post::class.java)
            if (post != null) {
                val likedUsers = post.likedUsers.toMutableMap()
                val isLiked = likedUsers.containsKey(userId)
                
                if (isLiked) {
                    // Unlike: Remove user from likedUsers and decrease likes count
                    likedUsers.remove(userId)
                    postRef.updateChildren(mapOf(
                        "likes" to post.likes - 1,
                        "likedUsers" to likedUsers
                    )).addOnSuccessListener {
                        result.value = Resource.success(true)
                    }.addOnFailureListener { e ->
                        result.value = Resource.error(e.message ?: "Error updating like status", null)
                    }
                } else {
                    // Like: Add user to likedUsers and increase likes count
                    likedUsers[userId] = true
                    postRef.updateChildren(mapOf(
                        "likes" to post.likes + 1,
                        "likedUsers" to likedUsers
                    )).addOnSuccessListener {
                        result.value = Resource.success(true)
                    }.addOnFailureListener { e ->
                        result.value = Resource.error(e.message ?: "Error updating like status", null)
                    }
                }
            } else {
                result.value = Resource.error("Post not found", null)
            }
        }.addOnFailureListener { e ->
            result.value = Resource.error(e.message ?: "Error fetching post", null)
        }

        return result
    }

    fun deletePost(postId: String): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        
        result.value = Resource.loading(null)

        postsRef.child(postId).removeValue()
            .addOnSuccessListener {
                result.value = Resource.success(true)
            }
            .addOnFailureListener { e ->
                result.value = Resource.error(e.message ?: "Error deleting post", null)
            }

        return result
    }
}