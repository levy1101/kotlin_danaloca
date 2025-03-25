package com.levy.danaloca.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.Post
import com.levy.danaloca.model.User
import com.levy.danaloca.repository.FriendsRepository
import com.levy.danaloca.repository.PostRepository
import com.levy.danaloca.repository.UserRepository
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.utils.Status

class UserProfileViewModel(context: Context) : ViewModel() {
    private val userRepository = UserRepository(context)
    private val friendsRepository = FriendsRepository()
    private val postRepository = PostRepository(context)

    private val _user = MutableLiveData<Resource<User>>(Resource.loading())
    val user: LiveData<Resource<User>> = _user

    private val _friendRequests = MutableLiveData<Resource<List<FriendRequest>>>(Resource.loading())
    val friendRequests: LiveData<Resource<List<FriendRequest>>> = _friendRequests

    private val _userPosts = MutableLiveData<Resource<List<Post>>>(Resource.loading())
    val userPosts: LiveData<Resource<List<Post>>> = _userPosts

    private val _operationState = MutableLiveData<Resource<Unit>?>(null)
    val operationState: LiveData<Resource<Unit>?> = _operationState

    fun loadUserProfile(userId: String) {
        userRepository.getUserProfile(userId) { result ->
            _user.postValue(result)
        }
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
            loadFriendRequests(currentUserId)
            if (currentUserId != userId) {
                loadFriendRequests(userId)
            }
        }
        loadUserPosts(userId)
    }

    private fun loadUserPosts(userId: String) {
        _userPosts.value = Resource.loading()
        Log.d("UserProfileViewModel", "Loading posts for user: $userId")
        postRepository.getPosts()
            .observeForever { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        val allPosts = resource.data
                        if (allPosts != null) {
                            val userPosts = allPosts.filter { it.userId == userId }
                            Log.d("UserProfileViewModel", "Found ${userPosts.size} posts for user $userId")
                            userPosts.forEach { post ->
                                Log.d("UserProfileViewModel", "Post: id=${post.id}, userId=${post.userId}, content=${post.content}")
                            }
                            _userPosts.postValue(Resource.success(userPosts))
                        } else {
                            Log.d("UserProfileViewModel", "No posts found")
                            _userPosts.postValue(Resource.success(emptyList()))
                        }
                    }
                    Status.ERROR -> {
                        Log.e("UserProfileViewModel", "Error loading posts: ${resource.message}")
                        _userPosts.postValue(resource)
                    }
                    Status.LOADING -> {
                        Log.d("UserProfileViewModel", "Loading posts...")
                    }
                }
            }
    }

    fun loadFriendRequests(userId: String) {
        friendsRepository.getFriendRequests(userId) { result ->
            _friendRequests.postValue(result)
        }
    }

    fun sendFriendRequest(currentUserId: String, userId: String) {
        friendsRepository.sendFriendRequest(currentUserId, userId) { result ->
            _operationState.postValue(result)
            loadFriendRequests(currentUserId)
            loadFriendRequests(userId)
            loadUserProfile(userId)
        }
    }

    fun cancelFriendRequest(requestId: String, userId: String) {
        friendsRepository.cancelFriendRequest(requestId) { result ->
            _operationState.postValue(result)
            FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
                loadFriendRequests(currentUserId)
                loadFriendRequests(userId)
            }
        }
    }

    fun declineFriendRequest(requestId: String, userId: String) {
        friendsRepository.declineFriendRequest(requestId) { result ->
            _operationState.postValue(result)
            FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserId ->
                loadFriendRequests(currentUserId)
                loadFriendRequests(userId)
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        friendsRepository.acceptFriendRequest(request) { result ->
            _operationState.postValue(result)
            loadUserProfile(request.receiverId)
            loadUserProfile(request.senderId)
            loadFriendRequests(request.receiverId)
            loadFriendRequests(request.senderId)
        }
    }

    fun removeFriend(currentUserId: String, userId: String) {
        friendsRepository.removeFriend(currentUserId, userId) { result ->
            _operationState.postValue(result)
            loadUserProfile(userId)
            loadFriendRequests(currentUserId)
            loadFriendRequests(userId)
        }
    }

    fun unfollowUser(currentUserId: String, userId: String) {
        friendsRepository.unfollowUser(currentUserId, userId) { result ->
            _operationState.postValue(result)
            loadUserProfile(userId)
            loadFriendRequests(currentUserId)
            loadFriendRequests(userId)
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    fun getFriendRequestId(senderId: String, receiverId: String): String {
        return "${senderId}_${receiverId}"
    }

    companion object {
        fun create(context: Context): UserProfileViewModel {
            return UserProfileViewModel(context)
        }
    }
}