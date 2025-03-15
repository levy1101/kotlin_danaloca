package com.levy.danaloca.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource
import java.util.UUID

class CreatePostViewModel : ViewModel() {
    private val postRepository = PostRepository()
    private val auth = FirebaseAuth.getInstance()

    private val createPostStatus = MutableLiveData<Resource<Boolean>>()
    fun getCreatePostStatus(): LiveData<Resource<Boolean>> = createPostStatus

    fun createPost(content: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            createPostStatus.value = Resource.error("User not logged in", false)
            return
        }

        if (content.isBlank()) {
            createPostStatus.value = Resource.error("Content cannot be empty", false)
            return
        }

        val post = Post(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        postRepository.createPost(post).observeForever { result ->
            createPostStatus.value = result
        }
    }

    fun createPostWithImage(content: String, bitmap: Bitmap) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            createPostStatus.value = Resource.error("User not logged in", false)
            return
        }

        if (content.isBlank()) {
            createPostStatus.value = Resource.error("Content cannot be empty", false)
            return
        }

        val post = Post(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        postRepository.createPostWithImage(post, bitmap).observeForever { result ->
            createPostStatus.value = result
        }
    }
}