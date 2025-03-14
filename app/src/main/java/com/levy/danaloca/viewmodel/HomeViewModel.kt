package com.levy.danaloca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource

class HomeViewModel : ViewModel() {
    private val postRepository = PostRepository()

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
        postRepository.getPosts().observeForever { result ->
            posts.value = result
        }
    }

    fun likePost(postId: String, currentLikes: Int) {
        postRepository.updateLikes(postId, currentLikes + 1).observeForever { result ->
            likeUpdateStatus.value = result
        }
    }

    fun deletePost(postId: String) {
        postRepository.deletePost(postId).observeForever { result ->
            deleteStatus.value = result
        }
    }

    fun refreshPosts() {
        loadPosts()
    }
}