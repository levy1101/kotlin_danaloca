package com.levy.danaloca.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.levy.danaloca.R
import com.levy.danaloca.utils.Status
import com.levy.danaloca.viewmodel.CreatePostViewModel

class CreatePostActivity : AppCompatActivity() {

    private lateinit var contentEditText: EditText
    private lateinit var backButton: ImageButton
    private lateinit var postButton: Button
    private val viewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        contentEditText = findViewById(R.id.contentEditText)
        backButton = findViewById(R.id.backButton)
        postButton = findViewById(R.id.postButton)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        postButton.setOnClickListener {
            val content = contentEditText.text.toString()
            viewModel.createPost(content)
            postButton.isEnabled = false
        }

        contentEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                contentEditText.hint = ""
            } else if (contentEditText.text.isBlank()) {
                contentEditText.hint = getString(R.string.whats_on_your_mind)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.getCreatePostStatus().observe(this) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    postButton.isEnabled = false
                }
                Status.SUCCESS -> {
                    Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Status.ERROR -> {
                    postButton.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}