package com.levy.danaloca.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val comments: Int = 0,
    val likedUsers: MutableMap<String, Boolean> = mutableMapOf()  // Map to store user IDs who liked the post
)