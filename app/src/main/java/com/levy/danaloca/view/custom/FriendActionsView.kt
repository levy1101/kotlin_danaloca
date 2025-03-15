package com.levy.danaloca.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.levy.danaloca.R
import com.levy.danaloca.databinding.LayoutFriendActionsBinding
import com.levy.danaloca.model.FriendRequest
import com.levy.danaloca.model.FriendRequestStatus
import com.levy.danaloca.model.User

class FriendActionsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: LayoutFriendActionsBinding
    private var onAddFriendClick: ((String) -> Unit)? = null
    private var onCancelRequestClick: ((String) -> Unit)? = null
    private var onAcceptClick: ((FriendRequest) -> Unit)? = null
    private var onDeclineClick: ((String, String) -> Unit)? = null
    private var onFriendOptionsClick: ((String, View) -> Unit)? = null
    
    private var currentUserId: String = ""
    private var targetUserId: String = ""

    init {
        binding = LayoutFriendActionsBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnAddFriend.setOnClickListener {
                onAddFriendClick?.invoke(targetUserId)
            }

            btnCancelRequest.setOnClickListener {
                onCancelRequestClick?.invoke(getFriendRequestId())
            }

            btnAccept.setOnClickListener {
                val request = receivedRequest // Store in local val
                if (request != null) {
                    onAcceptClick?.invoke(request.copy(status = FriendRequestStatus.ACCEPTED))
                }
            }

            btnDecline.setOnClickListener {
                val request = receivedRequest // Store in local val
                if (request != null) {
                    onDeclineClick?.invoke(request.id, targetUserId)
                }
            }

            btnFriendOptions.setOnClickListener { view ->
                onFriendOptionsClick?.invoke(targetUserId, view)
            }
        }
    }

    fun setAddFriendListener(listener: (String) -> Unit) {
        onAddFriendClick = listener
    }

    fun setCancelRequestListener(listener: (String) -> Unit) {
        onCancelRequestClick = listener
    }

    fun setAcceptRequestListener(listener: (FriendRequest) -> Unit) {
        onAcceptClick = listener
    }

    fun setDeclineRequestListener(listener: (String, String) -> Unit) {
        onDeclineClick = listener
    }

    fun setFriendOptionsListener(listener: (String, View) -> Unit) {
        onFriendOptionsClick = listener
    }

    fun setUserIds(currentUserId: String, targetUserId: String) {
        this.currentUserId = currentUserId
        this.targetUserId = targetUserId
    }

    private var receivedRequest: FriendRequest? = null

    fun updateState(user: User? = null, requests: List<FriendRequest>? = null) {
        if (currentUserId == targetUserId) {
            visibility = View.GONE
            return
        }

        visibility = View.VISIBLE
        
        // Safe check for null values
        val requestsList = requests ?: emptyList()
        
        // Check for any active request between these users
        val sentRequest = requestsList.find {
            it.senderId == currentUserId && it.receiverId == targetUserId
        }
        this.receivedRequest = requestsList.find {
            it.senderId == targetUserId && it.receiverId == currentUserId
        }

        // Store in local val for smart cast
        val currentReceivedRequest = receivedRequest
        
        binding.apply {
            when {
                // Show friend options if we are already friends
                user?.isFriendWith(currentUserId) == true -> {
                    showFriendsOptions()
                }
                // Show cancel request if we sent a request
                sentRequest != null && sentRequest.status == FriendRequestStatus.PENDING -> {
                    showCancelRequest()
                }
                // Show accept/decline if we received a pending request
                currentReceivedRequest != null && currentReceivedRequest.status == FriendRequestStatus.PENDING -> {
                    showReceivedRequest()
                }
                // Default: show add friend button
                else -> {
                    showAddFriend()
                }
            }
        }
    }

    private fun showAddFriend() {
        binding.apply {
            btnAddFriend.visibility = View.VISIBLE
            btnCancelRequest.visibility = View.GONE
            layoutRequestActions.visibility = View.GONE
            btnFriendOptions.visibility = View.GONE
        }
    }

    private fun showCancelRequest() {
        binding.apply {
            btnAddFriend.visibility = View.GONE
            btnCancelRequest.visibility = View.VISIBLE
            layoutRequestActions.visibility = View.GONE
            btnFriendOptions.visibility = View.GONE
        }
    }

    private fun showReceivedRequest() {
        binding.apply {
            btnAddFriend.visibility = View.GONE
            btnCancelRequest.visibility = View.GONE
            layoutRequestActions.visibility = View.VISIBLE
            btnFriendOptions.visibility = View.GONE
        }
    }

    private fun showFriendsOptions() {
        binding.apply {
            btnAddFriend.visibility = View.GONE
            btnCancelRequest.visibility = View.GONE
            layoutRequestActions.visibility = View.GONE
            btnFriendOptions.visibility = View.VISIBLE
        }
    }

    private fun getFriendRequestId(): String {
        return "${currentUserId}_${targetUserId}"
    }
}