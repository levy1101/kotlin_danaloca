package com.levy.danaloca.view.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import kotlinx.coroutines.launch

class MyProfileFragment : BaseProfileFragment() {

    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var birthdateText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var additionalInfoContainer: View

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.openInputStream(it)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)?.let { bitmap ->
                        progressBar.visibility = View.VISIBLE

                        lifecycleScope.launch {
                            try {
                                userViewModel.getCurrentUser()?.let { user ->
                                    val result = userViewModel.updateUserAvatar(user.uid, bitmap)
                                    when (result) {
                                        is Resource.Success -> {
                                            Glide.with(requireContext())
                                                .load(result.data)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .placeholder(R.drawable.default_avatar)
                                                .error(R.drawable.default_avatar)
                                                .circleCrop()
                                                .into(profileImageView)
                                            
                                            Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        is Resource.Error -> {
                                            Toast.makeText(requireContext(), result.message ?: "Failed to update profile image", Toast.LENGTH_LONG).show()
                                            loadUserData()
                                        }
                                        else -> {}
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Error updating profile image: ${e.message}", Toast.LENGTH_LONG).show()
                                loadUserData()
                            } finally {
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_myprofile, container, false)
    }

    private lateinit var titleText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdditionalViews(view)
    }

    private fun initAdditionalViews(view: View) {
        titleText = view.findViewById(R.id.settings_title)
        val profileInfoView = view.findViewById<View>(R.id.profile_info)
        
        emailText = profileInfoView.findViewById(R.id.tv_email)
        phoneText = profileInfoView.findViewById(R.id.tv_phone_number)
        birthdateText = profileInfoView.findViewById(R.id.tv_birthdate)
        additionalInfoContainer = profileInfoView.findViewById(R.id.ll_additional_info)
        additionalInfoContainer.visibility = View.VISIBLE
        progressBar = view.findViewById(R.id.progress_bar)
    }

    override fun onChangeAvatarClick() {
        pickImage.launch("image/*")
    }

    // Implement abstract methods from BaseProfileFragment
    override fun getGenderTextId() = R.id.tv_gender
    override fun getAgeTextId() = R.id.tv_age
    override fun getLocationTextId() = R.id.tv_location
    override fun getProfileImageId() = R.id.profile_image
    override fun getUsernameTextId() = R.id.tv_username

    override fun loadUserData() {
        userViewModel.getCurrentUser()?.let { firebaseUser ->
            userViewModel.getUser(firebaseUser.uid)
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                user?.let { updateProfileInfo(it) }
            }
            updateUserPosts(firebaseUser.uid)
        }
    }

    override fun updateProfileInfo(user: User) {
        super.updateProfileInfo(user)
        titleText.text = user.fullName
        emailText.text = user.email.ifBlank { "Not set" }
        phoneText.text = user.phoneNumber.ifBlank { "Not set" }
        birthdateText.text = user.birthdate.ifBlank { "Not set" }

        if (user.avatarUrl.isNotBlank()) {
            Glide.with(requireContext())
                .load(user.avatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.default_avatar)
        }
    }
}
