package com.levy.danaloca.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.view.ViewGroup
import android.widget.TextView
import com.levy.danaloca.utils.ImageUtils
import de.hdodenhof.circleimageview.CircleImageView
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource

class MyProfileFragment : PostFragment() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    // Profile info views
    private lateinit var fullNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var genderText: TextView
    private lateinit var ageText: TextView
    private lateinit var birthdateText: TextView
    private lateinit var locationText: TextView
    private lateinit var profileImageView: CircleImageView
    private lateinit var changeProfileButton: ImageButton

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
        
        changeProfileButton.setOnClickListener {
            openImagePicker()
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

        // Handle profile image
        user.avatar.let { base64Image ->
            if (base64Image.isNotBlank()) {
                ImageUtils.base64ToBitmap(base64Image)?.let { bitmap ->
                    profileImageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val imageUri = data.data
                imageUri?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    
                    // Convert to base64
                    val base64Image = ImageUtils.bitmapToBase64(bitmap)
                    
                    // Update profile image immediately
                    profileImageView.setImageBitmap(bitmap)
                    
                    // Update user avatar in Firebase
                    userViewModel.getCurrentUser()?.let { user ->
                        userViewModel.updateUserAvatar(user.uid, base64Image)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
