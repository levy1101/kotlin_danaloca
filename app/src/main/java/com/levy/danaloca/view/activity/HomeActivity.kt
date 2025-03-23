package com.levy.danaloca.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.levy.danaloca.R
import androidx.navigation.fragment.NavHostFragment
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navView: CurvedBottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        navView = findViewById(R.id.bottom_navigation)
        navView.setBackgroundColor(android.graphics.Color.TRANSPARENT) // Set background to transparent

        val menuItems = arrayOf(
            CbnMenuItem(
                R.drawable.ic_notifications,
                R.drawable.avd_notifications,
                R.id.nav_notifications
            ),
            CbnMenuItem(
                R.drawable.ic_friends,
                R.drawable.avd_friends,
                R.id.nav_friends
            ),
            CbnMenuItem(
                R.drawable.ic_home,
                R.drawable.avd_home,
                R.id.nav_home
            ),
            CbnMenuItem(
                R.drawable.ic_profile,
                R.drawable.avd_profile,
                R.id.nav_profile
            ),
            CbnMenuItem(
                R.drawable.ic_settings,
                R.drawable.avd_settings,
                R.id.nav_settings
            )
        )
        navView.setMenuItems(menuItems, 2)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    fun logout() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // Clear back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}