package com.levy.danaloca.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.levy.danaloca.R
import com.levy.danaloca.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            // Handle search action here
            true
        }
    }
}