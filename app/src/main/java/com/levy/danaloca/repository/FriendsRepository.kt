package com.levy.danaloca.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.FriendRequestStatus
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource

class FriendsRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val friendRequestsRef = database.getReference("friend_requests")
    private val usersRef = database.getReference("users")

    fun getFriendRequests(userId: String, callback: (Resource<List<FriendRequest>>) -> Unit) {
        callback(Resource.loading())

        // Create a map to store all requests
        val allRequests = mutableMapOf<String, FriendRequest>()
        var receiverLoaded = false
        var senderLoaded = false

        // Function to notify when both listeners have loaded
        fun notifyIfComplete() {
            if (receiverLoaded && senderLoaded) {
                callback(Resource.success(allRequests.values.toList()))
            }
        }

        // Listen for received requests
        friendRequestsRef
            .orderByChild("receiverId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val request = it.getValue(FriendRequest::class.java)
                        if (request != null) {
                            allRequests[request.id] = request
                        }
                    }
                    receiverLoaded = true
                    notifyIfComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Resource.error(error.message))
                }
            })

        // Listen for sent requests
        friendRequestsRef
            .orderByChild("senderId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val request = it.getValue(FriendRequest::class.java)
                        if (request != null) {
                            allRequests[request.id] = request
                        }
                    }
                    senderLoaded = true
                    notifyIfComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Resource.error(error.message))
                }
            })
    }

    fun getUsers(currentUserId: String, callback: (Resource<List<User>>) -> Unit) {
        callback(Resource.loading())

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { 
                    val user = it.getValue(User::class.java)
                    // Skip if it's the current user
                    if (it.key == currentUserId) return@mapNotNull null
                    
                    user?.copy(
                        id = it.key ?: "",
                        username = user.fullName // Use fullName as username if no username set
                    )
                }
                callback(Resource.success(users))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Resource.error(error.message))
            }
        })
    }

    fun sendFriendRequest(senderId: String, receiverId: String, callback: (Resource<Unit>) -> Unit) {
        val request = FriendRequest(
            id = "${senderId}_${receiverId}",
            senderId = senderId,
            receiverId = receiverId,
            status = FriendRequestStatus.PENDING
        )
        friendRequestsRef.child(request.id).setValue(request)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to send friend request")) }
    }

    fun cancelFriendRequest(requestId: String, callback: (Resource<Unit>) -> Unit) {
        // Update status to CANCELED instead of removing
        friendRequestsRef.child(requestId).child("status")
            .setValue(FriendRequestStatus.CANCELED)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to cancel friend request")) }
    }

    fun acceptFriendRequest(request: FriendRequest, callback: (Resource<Unit>) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "/users/${request.senderId}/friends/${request.receiverId}" to true,
            "/users/${request.receiverId}/friends/${request.senderId}" to true,
            "/friend_requests/${request.id}/status" to FriendRequestStatus.ACCEPTED
        )
        
        database.reference.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to accept friend request")) }
    }

    fun declineFriendRequest(requestId: String, callback: (Resource<Unit>) -> Unit) {
        friendRequestsRef.child(requestId).child("status")
            .setValue(FriendRequestStatus.DECLINED)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to decline friend request")) }
    }

    fun removeFriend(userId: String, friendId: String, callback: (Resource<Unit>) -> Unit) {
        val updates = hashMapOf<String, Any?>(
            "/users/$userId/friends/$friendId" to null,
            "/users/$friendId/friends/$userId" to null
        )
        
        database.reference.updateChildren(updates)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to remove friend")) }
    }

    fun unfollowUser(userId: String, friendId: String, callback: (Resource<Unit>) -> Unit) {
        usersRef.child(userId).child("friends").child(friendId).setValue(false)
            .addOnSuccessListener { callback(Resource.success(Unit)) }
            .addOnFailureListener { e -> callback(Resource.error(e.message ?: "Failed to unfollow user")) }
    }
}