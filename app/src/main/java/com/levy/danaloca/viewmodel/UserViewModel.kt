package com.levy.danaloca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.model.User
import com.levy.danaloca.repository.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun saveUser(user: User) {
        _isLoading.value = true
        repository.saveUser(user)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun getUser(email: String) {
        _isLoading.value = true
        repository.getUser(email).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.value = false
                snapshot.getValue(User::class.java)?.let {
                    _user.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _error.value = error.message
            }
        })
    }

    fun getUserByPhone(phone: String) {
        _isLoading.value = true
        repository.getUserByPhone(phone).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.value = false
                snapshot.children.firstOrNull()?.getValue(User::class.java)?.let {
                    _user.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _error.value = error.message
            }
        })
    }

    fun updateUser(email: String, updates: Map<String, Any>) {
        _isLoading.value = true
        repository.updateUser(email, updates)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun deleteUser(email: String) {
        _isLoading.value = true
        repository.deleteUser(email)
            .addOnSuccessListener {
                _isLoading.value = false
                _user.value = null
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }
}