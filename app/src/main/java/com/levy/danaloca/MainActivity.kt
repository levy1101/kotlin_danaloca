package com.levy.danaloca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.view.HomeActivity
import com.levy.danaloca.view.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        val logo = findViewById<ImageView>(R.id.app_logo)
        val title = findViewById<TextView>(R.id.app_title)
        val animationView = findViewById<LottieAnimationView>(R.id.loading_animation)

        // Setup animations
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            fillAfter = true
        }

        val fadeInDelayed = AlphaAnimation(0f, 1f).apply {
            startOffset = 500
            duration = 1000
            fillAfter = true
        }

        // Start animations
        logo.startAnimation(fadeIn)
        title.startAnimation(fadeInDelayed)
        
        // Setup Lottie animation
        animationView.setAnimation("loading_animation.json")
        animationView.playAnimation()

        // Check auth state and navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                navigateToHome()
            } else {
                navigateToLogin()
            }
        }, 3000) // Display splash screen for 3 seconds
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}