package com.levy.danaloca.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.levy.danaloca.R
import com.levy.danaloca.utils.Status
import com.levy.danaloca.viewmodel.CreatePostViewModel

class CreatePostActivity : AppCompatActivity() {

    private lateinit var contentEditText: EditText
    private lateinit var backButton: ImageButton
    private lateinit var addImageButton: ImageButton
    private lateinit var addLocationButton: ImageButton
    private lateinit var imagePreview: ImageView
    private lateinit var locationText: TextView
    private lateinit var postButton: Button
    private val viewModel: CreatePostViewModel by viewModels()
    
    private var selectedImage: Bitmap? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private val pickLocation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedLatitude = data.getDoubleExtra(MapLocationActivity.EXTRA_LATITUDE, 0.0)
                selectedLongitude = data.getDoubleExtra(MapLocationActivity.EXTRA_LONGITUDE, 0.0)
                locationText.text = "Location: $selectedLatitude, $selectedLongitude"
                locationText.visibility = View.VISIBLE
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    imagePreview.setImageBitmap(selectedImage)
                    imagePreview.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
    addImageButton = findViewById(R.id.addImageButton)
    addLocationButton = findViewById(R.id.addLocationButton)
    imagePreview = findViewById(R.id.imagePreview)
    locationText = findViewById(R.id.locationText)
    postButton = findViewById(R.id.postButton)
}

private fun setupListeners() {
    backButton.setOnClickListener {
        finish()
    }

    addImageButton.setOnClickListener {
        checkPermissionAndOpenPicker()
    }

    addLocationButton.setOnClickListener {
        pickLocation.launch(Intent(this, MapLocationActivity::class.java))
    }

    postButton.setOnClickListener {
        val content = contentEditText.text.toString()
        selectedImage?.let { bitmap ->
            viewModel.createPostWithImage(
                content = content,
                bitmap = bitmap,
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )
        } ?: viewModel.createPost(
            content = content,
            latitude = selectedLatitude,
            longitude = selectedLongitude
        )
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

    private fun checkPermissionAndOpenPicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
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