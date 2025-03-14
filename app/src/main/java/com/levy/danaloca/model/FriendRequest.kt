package com.levy.danaloca.model

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING
)

enum class FriendRequestStatus {
    PENDING,   // Request sent but not accepted
    RECEIVED,  // Request received but not acted upon
    ACCEPTED,  // Request accepted (users are now friends)
    DECLINED,  // Request declined
    CANCELED   // Request canceled by sender
}