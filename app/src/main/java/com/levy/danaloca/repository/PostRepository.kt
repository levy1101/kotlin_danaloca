package com.levy.danaloca.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.model.Post
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostRepository(private val context: Context) {
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

    fun getUserPosts(userId: String): MutableLiveData<Resource<List<Post>>> {
        val result = MutableLiveData<Resource<List<Post>>>()
        
        result.value = Resource.loading(null)
        Log.d("PostRepository", "Starting to fetch posts for user: $userId")
        
        postsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("PostRepository", "Received snapshot with ${snapshot.childrenCount} posts")
                    val posts = mutableListOf<Post>()
                    for (postSnapshot in snapshot.children) {
                        try {
                            postSnapshot.getValue(Post::class.java)?.let { post ->
                                Log.d("PostRepository", "Found post: id=${post.id}, userId=${post.userId}")
                                posts.add(post)
                            } ?: Log.e("PostRepository", "Failed to parse post from snapshot: ${postSnapshot.value}")
                        } catch (e: Exception) {
                            Log.e("PostRepository", "Error parsing post: ${e.message}, snapshot: ${postSnapshot.value}")
                        }
                    }
                    posts.sortByDescending { it.timestamp }
                    Log.d("PostRepository", "Successfully processed ${posts.size} posts for user $userId")
                    result.value = Resource.success(posts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PostRepository", "Error fetching posts: ${error.message}, code: ${error.code}, details: ${error.details}")
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

    fun createPostWithImage(post: Post, bitmap: Bitmap): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        result.value = Resource.loading(null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload image to Cloudinary
                val imageUrl = ImageUtils.uploadImage(context, bitmap, "posts")
                
                // Create new post with image URL
                val postWithImage = post.copy(imageUrl = imageUrl)
                
                // Save to Firebase
                postsRef.child(post.id).setValue(postWithImage)
                    .addOnSuccessListener {
                        result.postValue(Resource.success(true))
                    }
                    .addOnFailureListener { e ->
                        result.postValue(Resource.error(e.message ?: "Error creating post with image", null))
                    }
            } catch (e: Exception) {
                result.postValue(Resource.error("Error uploading image: ${e.message}", null))
            }
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