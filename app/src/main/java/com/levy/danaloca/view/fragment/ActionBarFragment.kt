package com.levy.danaloca.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.card.MaterialCardView
import com.levy.danaloca.R

class ActionBarFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var addButton: FloatingActionButton

    interface ActionBarListener {
        fun onSearchClicked()
        fun onAddClicked()
    }

    private var listener: ActionBarListener? = null

    fun setActionBarListener(listener: ActionBarListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_action_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        addButton = view.findViewById(R.id.addButton)

        setupListeners()
    }

    private fun setupListeners() {
        searchEditText.setOnClickListener {
            listener?.onSearchClicked()
        }

        addButton.setOnClickListener {
            listener?.onAddClicked()
        }

    }
}
