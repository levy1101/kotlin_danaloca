package com.levy.danaloca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.NotificationCompat
import com.levy.danaloca.view.HomeActivity
import com.levy.danaloca.view.LoginActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CHANNEL_ID = "danaloca_default_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        val logo = findViewById<ImageView>(R.id.app_logo)
        val title = findViewById<TextView>(R.id.app_title)
        val animationView = findViewById<LottieAnimationView>(R.id.loading_animation)
        val testButton = findViewById<Button>(R.id.test_notification_button)

        // Create notification channel
        createNotificationChannel()

        // Test notification button
        testButton.setOnClickListener {
            showTestNotification()
        }

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
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
        finish()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val description = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            this.description = description
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showTestNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Test Notification")
            .setContentText("Click to open DanaLoca app")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}