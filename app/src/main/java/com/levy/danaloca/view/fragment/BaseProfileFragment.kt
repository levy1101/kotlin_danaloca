package com.levy.danaloca.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.GlideUtils
import de.hdodenhof.circleimageview.CircleImageView

abstract class BaseProfileFragment : PostFragment() {

    protected lateinit var genderText: TextView
    protected lateinit var ageText: TextView
    protected lateinit var locationText: TextView
    protected lateinit var usernameText: TextView
    protected lateinit var profileImageView: CircleImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBaseProfileViews(view)
        loadUserData()
    }

    protected open fun initBaseProfileViews(view: View) {
        genderText = view.findViewById(getGenderTextId())
        ageText = view.findViewById(getAgeTextId())
        locationText = view.findViewById(getLocationTextId())
        usernameText = view.findViewById(getUsernameTextId())
        profileImageView = view.findViewById(getProfileImageId())
        
        profileImageView.setOnClickListener {
            showAvatarPreviewDialog(this is MyProfileFragment)
        }
    }

    protected open fun showAvatarPreviewDialog(isMyProfile: Boolean) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_avatar_preview, null)
        dialog.setContentView(dialogView)

        val avatarPreview = dialogView.findViewById<ImageView>(R.id.iv_avatar_preview)
        val changeAvatarButton = dialogView.findViewById<Button>(R.id.btn_change_avatar)

        // Load current avatar
        val user = userViewModel.user.value
        GlideUtils.loadProfileImage(avatarPreview, user?.avatarUrl)

        // Show change avatar button only in MyProfile
        changeAvatarButton.visibility = if (isMyProfile) View.VISIBLE else View.GONE
        
        // Handle change avatar button click in MyProfileFragment
        if (isMyProfile) {
            changeAvatarButton.setOnClickListener {
                dialog.dismiss()
                onChangeAvatarClick()
            }
        }

        // Dismiss dialog when clicking outside the image
        dialogView.setOnClickListener {
            dialog.dismiss()
        }

        // Prevent avatar preview click from dismissing
        avatarPreview.setOnClickListener { }

        dialog.show()
    }

    protected open fun onChangeAvatarClick() {
        // To be implemented by MyProfileFragment
    }

    // Abstract methods for view IDs since they might be different in different layouts
    abstract fun getGenderTextId(): Int
    abstract fun getAgeTextId(): Int
    abstract fun getLocationTextId(): Int
    abstract fun getUsernameTextId(): Int
    abstract fun getProfileImageId(): Int

    override fun getRecyclerViewId() = R.id.posts_recycler_view
    override fun getSwipeRefreshId() = R.id.swipeRefreshLayout

    override fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadUserData()
        }
    }

    abstract fun loadUserData()

    protected open fun updateProfileInfo(user: User) {
        genderText.text = user.gender.ifBlank { "Not set" }
        ageText.text = user.age.ifBlank { "Not set" }
        locationText.text = user.location.ifBlank { "Not set" }
        usernameText.text = "@${user.username}".ifBlank { "@username" }
    }

    protected fun updateUserPosts(userId: String) {
        observePosts { posts ->
            val userPosts = posts.filter { it.userId == userId }
            postAdapter.updatePosts(userPosts)
        }
    }
}