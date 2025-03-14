package com.levy.danaloca.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.levy.danaloca.R
import com.levy.danaloca.databinding.ActivityCreatePostBinding

class CreatePostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.postButton.setOnClickListener {
            val content = binding.contentEditText.text.toString()
            // Handle post creation here
            finish()
        }
    }
}