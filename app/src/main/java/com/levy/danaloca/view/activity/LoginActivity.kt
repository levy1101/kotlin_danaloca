package com.levy.danaloca.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import com.levy.danaloca.viewmodel.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: UserViewModel
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var welcomeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Check if user is already signed in
        if (auth.currentUser != null) {
            navigateToHome()
            return
        }

        initializeViews()
        setupObservers()
        setupListeners()
    }

    private fun initializeViews() {
        welcomeText = findViewById(R.id.welcome_text)
        emailLayout = findViewById(R.id.email_layout)
        passwordLayout = findViewById(R.id.password_layout)
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        progressBar = findViewById(R.id.progress_bar)
        loginButton = findViewById(R.id.login_button)

        startTypewriterAnimation()
        startFadeInAnimations()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        val registerPrompt = findViewById<TextView>(R.id.register_prompt)
        registerPrompt.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun startTypewriterAnimation() {
        val text = "Welcome to Danaloca"
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

    private fun startFadeInAnimations() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 1000
        }

        emailLayout.startAnimation(fadeIn)
        passwordLayout.startAnimation(fadeIn)
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Enter your password"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        // Clear back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
    }
}