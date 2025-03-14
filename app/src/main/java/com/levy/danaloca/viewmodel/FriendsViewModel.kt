package com.levy.danaloca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.User
import com.levy.danaloca.repository.FriendsRepository
import com.levy.danaloca.utils.Resource

class FriendsViewModel(
    private val repository: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _users = MutableLiveData<Resource<List<User>>>(Resource.loading())
    val users: LiveData<Resource<List<User>>> = _users

    private val _friendRequests = MutableLiveData<Resource<List<FriendRequest>>>(Resource.loading())
    val friendRequests: LiveData<Resource<List<FriendRequest>>> = _friendRequests

    private val _operationState = MutableLiveData<Resource<Unit>?>(null)
    val operationState: LiveData<Resource<Unit>?> = _operationState

    fun loadUsers(currentUserId: String) {
        repository.getUsers(currentUserId) { result ->
            _users.postValue(result)
        }
    }

    fun loadFriendRequests(userId: String) {
        repository.getFriendRequests(userId) { result ->
            _friendRequests.postValue(result)
        }
    }

    fun sendFriendRequest(senderId: String, receiverId: String) {
        repository.sendFriendRequest(senderId, receiverId) { result ->
            _operationState.postValue(result)
            // Reload friend requests to update UI immediately
            loadFriendRequests(senderId)
        }
    }

    fun cancelFriendRequest(requestId: String, userId: String) {
        repository.cancelFriendRequest(requestId) { result ->
            _operationState.postValue(result)
            // Reload friend requests to update UI immediately
            loadFriendRequests(userId)
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        repository.acceptFriendRequest(request) { result ->
            _operationState.postValue(result)
            // Reload both friend requests and users to update UI immediately
            loadFriendRequests(request.receiverId)
            loadUsers(request.receiverId)
        }
    }

    fun removeFriend(userId: String, friendId: String) {
        repository.removeFriend(userId, friendId) { result ->
            _operationState.postValue(result)
            loadUsers(userId)
        }
    }

    fun blockUser(userId: String, blockedUserId: String) {
        repository.blockUser(userId, blockedUserId) { result ->
            _operationState.postValue(result)
            loadUsers(userId)
        }
    }

    fun unfollowUser(userId: String, friendId: String) {
        repository.unfollowUser(userId, friendId) { result ->
            _operationState.postValue(result)
            loadUsers(userId)
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    fun getFriendRequestId(senderId: String, receiverId: String): String {
        return "${senderId}_${receiverId}"
    }
}