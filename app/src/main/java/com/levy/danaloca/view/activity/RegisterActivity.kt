package com.levy.danaloca.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.model.User
import com.levy.danaloca.viewmodel.UserViewModel
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: UserViewModel
    private val defaultAvatarUrl = "https://res.cloudinary.com/dbftf4xyv/image/upload/v1742882579/avatar/default_avatar.jpg"
    private lateinit var emailLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var locationLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var phoneField: TextInputEditText
    private lateinit var fullNameField: TextInputEditText
    private lateinit var locationField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var welcomeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        
        initializeViews()
        setupObservers()
        setupListeners()
    }

    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcome_text)
        emailLayout = findViewById(R.id.email_layout)
        phoneLayout = findViewById(R.id.phone_layout)
        fullNameLayout = findViewById(R.id.full_name_layout)
        locationLayout = findViewById(R.id.location_layout)
        passwordLayout = findViewById(R.id.password_layout)
        
        emailField = findViewById(R.id.email)
        phoneField = findViewById(R.id.phone)
        fullNameField = findViewById(R.id.full_name)
        locationField = findViewById(R.id.location)
        passwordField = findViewById(R.id.password)
        
        progressBar = findViewById(R.id.progress_bar)
        registerButton = findViewById(R.id.register_button)

        startTypewriterAnimation()
        startSequentialFadeIn()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            registerButton.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        val birthdatePicker = findViewById<DatePicker>(R.id.birthdate)
        // Initialize date picker with current date
        birthdatePicker.init(birthdatePicker.year, birthdatePicker.month, birthdatePicker.dayOfMonth) { _, _, _, _ ->
            // Date changed
        }

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val fullName = fullNameField.text.toString().trim()
            val location = locationField.text.toString().trim()
            val password = passwordField.text.toString()
            val birthdate = "${birthdatePicker.dayOfMonth}/${birthdatePicker.month + 1}/${birthdatePicker.year}"
            val age = calculateAge(Calendar.getInstance().apply {
                set(birthdatePicker.year, birthdatePicker.month, birthdatePicker.dayOfMonth)
            }.timeInMillis).toString()

            // Get selected gender
            val selectedGenderId = findViewById<RadioGroup>(R.id.gender_group).checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.male -> "Male"
                R.id.female -> "Female"
                R.id.other -> "Others"
                else -> ""
            }

            if (validateInput(email, password, phone, fullName, gender, location, age)) {
                registerUser(email, password, phone, fullName, gender, location, birthdate, age)
            }
        }

        val loginPrompt = findViewById<TextView>(R.id.login_prompt)
        loginPrompt.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun startTypewriterAnimation() {
        val text = "Create Account"
        welcomeText.text = ""
        
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val characterDelay = 100L

        val runnable = object : Runnable {
            override fun run() {
                if (index < text.length) {
                    welcomeText.text = text.substring(0, index + 1)
                    index++
                    handler.postDelayed(this, characterDelay)
                }
            }
        }
        handler.postDelayed(runnable, characterDelay)
    }

    private fun startSequentialFadeIn() {
        val fields = listOf(
            emailLayout,
            passwordLayout,
            phoneLayout,
            fullNameLayout,
            locationLayout
        )

        fields.forEachIndexed { index, view ->
            Handler(Looper.getMainLooper()).postDelayed({
                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 500
                    fillAfter = true
                }
                view.startAnimation(fadeIn)
            }, (index + 1) * 200L)
        }
    }

    private fun calculateAge(birthdateInMillis: Long): Int {
        val birthdate = Calendar.getInstance()
        birthdate.timeInMillis = birthdateInMillis
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthdate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthdate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun validateInput(email: String, password: String, phone: String, fullName: String, gender: String, location: String, age: String?): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty() || password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        if (phone.isEmpty() || !android.util.Patterns.PHONE.matcher(phone).matches()) {
            phoneLayout.error = "Enter a valid phone number"
            isValid = false
        } else {
            phoneLayout.error = null
        }

        if (fullName.isEmpty()) {
            fullNameLayout.error = "Enter your full name"
            isValid = false
        } else {
            fullNameLayout.error = null
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (location.isEmpty()) {
            locationLayout.error = "Enter your location"
            isValid = false
        } else {
            locationLayout.error = null
        }

        return isValid
    }

    private fun registerUser(email: String, password: String, phone: String, fullName: String, 
                            gender: String, location: String, birthdate: String, age: String) {
        progressBar.visibility = View.VISIBLE
        registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = User(
                        id = auth.currentUser?.uid ?: "",
                        username = fullName.lowercase().replace(" ", ""),
                        email = email,
                        fullName = fullName,
                        phoneNumber = phone,
                        gender = gender,
                        location = location,
                        birthdate = birthdate,
                        age = age,
                        avatarUrl = defaultAvatarUrl
                    )
                    viewModel.saveUser(user)
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                }
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }
}
