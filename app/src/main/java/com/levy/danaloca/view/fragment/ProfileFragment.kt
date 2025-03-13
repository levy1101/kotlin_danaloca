package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.repository.UserRepository
import com.levy.danaloca.view.HomeActivity

class ProfileFragment : Fragment() {

    private lateinit var userRepository: UserRepository
    private var currentUserEmail: String? = null

    // UI Elements
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvBirthdate: TextView
    private lateinit var tvLocation: TextView
    private lateinit var btnEditProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize repository
        userRepository = UserRepository()

        // Get current user email
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        // Initialize UI elements
        initViews(view)

        // Set up listeners
        setupListeners()

        // Load user data
        loadUserData()

        return view
    }

    private fun initViews(view: View) {
        tvFullName = view.findViewById(R.id.tv_full_name)
        tvEmail = view.findViewById(R.id.tv_email)
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number)
        tvGender = view.findViewById(R.id.tv_gender)
        tvAge = view.findViewById(R.id.tv_age)
        tvBirthdate = view.findViewById(R.id.tv_birthdate)
        tvLocation = view.findViewById(R.id.tv_location)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            // Navigate to edit profile screen (to be implemented)
            Toast.makeText(requireContext(), "Edit profile clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        if (currentUserEmail.isNullOrEmpty()) {
            showError("User not logged in")
            return
        }

        userRepository.getUser(currentUserEmail!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        displayUserData(it)
                    } ?: showError("User data is null")
                } else {
                    showError("User data not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Error loading user data: ${error.message}")
                Log.e("ProfileFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun displayUserData(user: User) {
        tvFullName.text = user.fullName
        tvEmail.text = user.email
        tvPhoneNumber.text = user.phoneNumber
        tvGender.text = user.gender
        tvAge.text = user.age
        tvBirthdate.text = user.birthdate
        tvLocation.text = user.location
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.e("ProfileFragment", message)
    }
}