package com.levy.danaloca.view.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.hdodenhof.circleimageview.CircleImageView
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import kotlinx.coroutines.launch

class MyProfileFragment : PostFragment() {

    private lateinit var fullNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var genderText: TextView
    private lateinit var ageText: TextView
    private lateinit var birthdateText: TextView
    private lateinit var locationText: TextView
    private lateinit var profileImageView: CircleImageView
    private lateinit var changeProfileButton: ImageButton
    private lateinit var progressBar: ProgressBar

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.openInputStream(it)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)?.let { bitmap ->
                        // Show loading indicator
                        progressBar.visibility = View.VISIBLE
                        changeProfileButton.isEnabled = false

                        // Start upload process
                        lifecycleScope.launch {
                            try {
                                userViewModel.getCurrentUser()?.let { user ->
                                    val result = userViewModel.updateUserAvatar(user.uid, bitmap)
                                    when (result) {
                                        is Resource.Success -> {
                                            // Update image view with new image
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
                                            // Reload current user data to restore previous image
                                            loadUserData()
                                        }
                                        else -> {
                                            // Handle loading state if needed
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Error updating profile image: ${e.message}", Toast.LENGTH_LONG).show()
                                loadUserData() // Restore previous state
                            } finally {
                                progressBar.visibility = View.GONE
                                changeProfileButton.isEnabled = true
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initProfileViews(view)
        loadUserData()
    }

    private fun initProfileViews(view: View) {
        fullNameText = view.findViewById(R.id.tv_full_name)
        emailText = view.findViewById(R.id.tv_email)
        phoneText = view.findViewById(R.id.tv_phone_number)
        genderText = view.findViewById(R.id.tv_gender)
        ageText = view.findViewById(R.id.tv_age)
        birthdateText = view.findViewById(R.id.tv_birthdate)
        locationText = view.findViewById(R.id.tv_location)
        profileImageView = view.findViewById(R.id.profile_image)
        changeProfileButton = view.findViewById(R.id.btn_change_profile)
        progressBar = view.findViewById(R.id.progress_bar)
        
        changeProfileButton.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    override fun getRecyclerViewId() = R.id.posts_recycler_view
    override fun getSwipeRefreshId() = R.id.swipeRefreshLayout

    override fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadUserData()
        }
    }

    private fun loadUserData() {
        userViewModel.getCurrentUser()?.let { firebaseUser ->
            // Get user details
            userViewModel.getUser(firebaseUser.uid)
            userViewModel.user.observe(viewLifecycleOwner) { user ->
                user?.let { updateProfileInfo(it) }
            }

            // Observe posts from parent class
            observePosts { posts ->
                val userPosts = posts.filter { it.userId == firebaseUser.uid }
                postAdapter.updatePosts(userPosts)
            }
        }
    }

    private fun updateProfileInfo(user: User) {
        fullNameText.text = user.fullName.ifBlank { "Not set" }
        emailText.text = user.email.ifBlank { "Not set" }
        phoneText.text = user.phoneNumber.ifBlank { "Not set" }
        genderText.text = user.gender.ifBlank { "Not set" }
        ageText.text = user.age.ifBlank { "Not set" }
        birthdateText.text = user.birthdate.ifBlank { "Not set" }
        locationText.text = user.location.ifBlank { "Not set" }

        // Load profile image using Glide
        if (user.avatarUrl.isNotBlank()) {
            Glide.with(requireContext())
                .load(user.avatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Skip disk cache
                .skipMemoryCache(true) // Skip memory cache
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.default_avatar)
        }
    }
}
