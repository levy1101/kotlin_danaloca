package com.levy.danaloca.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepository(application)
    private val _posts = MutableLiveData<Resource<List<Post>>>()

    fun getPosts(): LiveData<Resource<List<Post>>> {
        _posts.value = Resource.loading(null)
        postRepository.getPosts().observeForever { result ->
            _posts.value = result
        }
        return _posts
    }

    fun refreshPosts() {
        _posts.value = Resource.loading(null)
        postRepository.getPosts().observeForever { result ->
            _posts.value = result
        }
    }

    fun toggleLike(postId: String, userId: String) {
        postRepository.toggleLike(postId, userId)
    }

    fun isPostLikedByUser(post: Post, userId: String): Boolean {
        return post.likedUsers.containsKey(userId)
    }
}