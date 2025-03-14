package com.levy.danaloca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource

class HomeViewModel : ViewModel() {
    private val postViewModel = PostViewModel()

    private val posts = MutableLiveData<Resource<List<Post>>>()
    fun getPosts(): LiveData<Resource<List<Post>>> = posts

    private val likeUpdateStatus = MutableLiveData<Resource<Boolean>>()
    fun getLikeUpdateStatus(): LiveData<Resource<Boolean>> = likeUpdateStatus

    private val deleteStatus = MutableLiveData<Resource<Boolean>>()
    fun getDeleteStatus(): LiveData<Resource<Boolean>> = deleteStatus

    init {
        loadPosts()
    }

    fun loadPosts() {
        postViewModel.getPosts()
        postViewModel.posts.observeForever { result ->
            posts.value = result
        }
    }

    fun toggleLike(postId: String, userId: String) {
        postViewModel.toggleLike(postId, userId)
        postViewModel.likeStatus.observeForever { result ->
            likeUpdateStatus.value = result
        }
    }

    fun isPostLikedByUser(post: Post, userId: String): Boolean {
        return postViewModel.isPostLikedByUser(post, userId)
    }

    fun deletePost(postId: String) {
        deleteStatus.value = Resource.loading(null)
        posts.value?.data?.find { it.id == postId }?.let { post ->
            postViewModel.deletePost(postId).observeForever { result ->
                deleteStatus.value = result
            }
        } ?: run {
            deleteStatus.value = Resource.error("Post not found", null)
        }
    }

    fun refreshPosts() {
        loadPosts()
    }
}