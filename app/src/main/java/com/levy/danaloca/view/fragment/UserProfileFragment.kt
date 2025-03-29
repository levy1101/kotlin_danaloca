package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.levy.danaloca.utils.GlideUtils
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.view.custom.FriendActionsView
import com.levy.danaloca.viewmodel.FriendsViewModel

class UserProfileFragment : BaseProfileFragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()

    private lateinit var profileTitle: TextView
    private lateinit var backButton: ImageButton
    private lateinit var messageButton: ImageButton
    private lateinit var friendActions: FriendActionsView
    private var additionalInfoContainer: View? = null

    private var currentUserId: String? = null
    private var userId: String? = null

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): UserProfileFragment {
            return UserProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_userprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        userId = arguments?.getString(ARG_USER_ID)
        
        if (userId == null) {
            parentFragmentManager.popBackStack()
            return
        }

        super.onViewCreated(view, savedInstanceState)
        initAdditionalViews(view)
        setupFriendActions()
        setupMessageButton()
        hideAdditionalInfo()
    }

    private fun hideAdditionalInfo() {
        additionalInfoContainer?.visibility = View.GONE
    }

    private fun initAdditionalViews(view: View) {
        profileTitle = view.findViewById(R.id.profile_title)
        backButton = view.findViewById(R.id.btn_back)
        messageButton = view.findViewById(R.id.btn_message)
        friendActions = view.findViewById(R.id.friend_actions)
        
        val profileInfoView = view.findViewById<View>(R.id.profile_info)
        additionalInfoContainer = profileInfoView.findViewById(R.id.ll_additional_info)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // Implement abstract methods from BaseProfileFragment
    override fun getGenderTextId() = R.id.tv_gender
    override fun getAgeTextId() = R.id.tv_age
    override fun getLocationTextId() = R.id.tv_location
    override fun getProfileImageId() = R.id.user_profile_image
    override fun getUsernameTextId() = R.id.tv_username

    private fun setupMessageButton() {
        messageButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            findNavController().navigate(
                R.id.action_userProfileFragment_to_chatFragment,
                bundle
            )
        }
    }

    private fun setupFriendActions() {
        currentUserId?.let { currentId ->
            userId?.let { profileId ->
                friendActions.apply {
                    setUserIds(currentId, profileId)
                    setAddFriendListener { friendsViewModel.sendFriendRequest(currentId, it) }
                    setCancelRequestListener { requestId -> friendsViewModel.cancelFriendRequest(requestId, profileId) }
                    setAcceptRequestListener { request -> friendsViewModel.acceptFriendRequest(request) }
                    setDeclineRequestListener { requestId, userId -> friendsViewModel.declineFriendRequest(requestId, userId) }
                    setFriendOptionsListener { userId, view ->
                        showFriendOptionsMenu(userId, view)
                    }
                }
            }
        }
    }

    private fun showFriendOptionsMenu(userId: String, anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_friend_options, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_unfollow -> {
                        friendsViewModel.unfollowUser(currentUserId!!, userId)
                        true
                    }
                    R.id.action_remove -> {
                        friendsViewModel.removeFriend(currentUserId!!, userId)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun loadUserData() {
        userId?.let { id ->
            userViewModel.getUser(id)
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                user?.let { updateProfileInfo(it) }
            }

            currentUserId?.let { currentId ->
                friendsViewModel.loadFriendRequests(currentId)
                friendsViewModel.friendRequests.observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            userViewModel.user.value?.let { user ->
                                friendActions.updateState(user, resource.data)
                            }
                        }
                        else -> {}
                    }
                }
            }

            friendsViewModel.operationState.observe(viewLifecycleOwner) { result ->
                result?.let {
                    when (it) {
                        is Resource.Success -> {
                            currentUserId?.let { currentId ->
                                friendsViewModel.loadFriendRequests(currentId)
                            }
                        }
                        else -> {}
                    }
                    friendsViewModel.resetOperationState()
                }
            }
            
            updateUserPosts(id)
        }
    }

    override fun updateProfileInfo(user: User) {
        super.updateProfileInfo(user)
        profileTitle.text = user.fullName
        GlideUtils.loadProfileImage(profileImageView, user.avatarUrl)
    }
}