package com.levy.danaloca.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levy.danaloca.model.Post
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.utils.Resource

class PostViewModel(context: Context) : ViewModel() {
    private val postRepository = PostRepository(context)
    private val _posts = MutableLiveData<Resource<List<Post>>>()
    val posts: LiveData<Resource<List<Post>>> = _posts

    private val _likeStatus = MutableLiveData<Resource<Boolean>>()
    val likeStatus: LiveData<Resource<Boolean>> = _likeStatus

    fun getPosts() {
        _posts.value = Resource.loading(null)
        postRepository.getPosts().observeForever { result ->
            _posts.value = result
        }
    }

    fun toggleLike(postId: String, userId: String) {
        _likeStatus.value = Resource.loading(null)
        postRepository.toggleLike(postId, userId).observeForever { result ->
            _likeStatus.value = result
        }
    }

    fun isPostLikedByUser(post: Post, userId: String): Boolean {
        return post.likedUsers.containsKey(userId)
    }

    fun deletePost(postId: String): LiveData<Resource<Boolean>> {
        return postRepository.deletePost(postId)
    }

    companion object {
        fun create(context: Context): PostViewModel {
            return PostViewModel(context)
        }
    }
}