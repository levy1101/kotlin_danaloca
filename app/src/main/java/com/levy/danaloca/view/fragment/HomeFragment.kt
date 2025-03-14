package com.levy.danaloca.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.levy.danaloca.databinding.FragmentHomeBinding
import com.levy.danaloca.view.CreatePostActivity
import com.levy.danaloca.view.MessagesActivity
import com.levy.danaloca.view.SearchActivity

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.searchEditText.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        binding.addButton.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }

        binding.messageButton.setOnClickListener {
            startActivity(Intent(requireContext(), MessagesActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}