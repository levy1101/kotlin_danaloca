package com.levy.danaloca.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.levy.danaloca.model.User
import com.levy.danaloca.repository.UserRepository
import com.levy.danaloca.utils.Resource
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)
    
    @ExperimentalCoroutinesApi
    suspend fun GetUserAvatar(userId: String): String {
        return suspendCancellableCoroutine { continuation ->
            repository.getUser(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        continuation.resume("") { }
                        return
                    }
                    
                    val user = snapshot.getValue(User::class.java)
                    if (user == null) {
                        continuation.resume("") { }
                        return
                    }

                    continuation.resume(user.avatarUrl) { }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume("") { }
                }
            })
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun GetUserFullName(userId: String): String {
        return suspendCancellableCoroutine { continuation ->
            Log.d("UserViewModel", "Getting user name for ID: $userId")
            repository.getUser(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d("UserViewModel", "No data exists for user ID: $userId")
                        continuation.resume("Unknown User") { }
                        return
                    }
                    
                    val user = snapshot.getValue(User::class.java)
                    Log.d("UserViewModel", "User data received: ${user?.toString()}")
                    
                    if (user == null) {
                        Log.d("UserViewModel", "Failed to parse user data for ID: $userId")
                        continuation.resume("Unknown User") { }
                        return
                    }

                    if (user.fullName.isBlank()) {
                        Log.d("UserViewModel", "User has blank name: $userId")
                        continuation.resume("Unknown User") { }
                        return
                    }
                    
                    Log.d("UserViewModel", "Found user name: ${user.fullName} for ID: $userId")
                    continuation.resume(user.fullName) { }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserViewModel", "Error getting user data: ${error.message}", error.toException())
                    continuation.resume("Unknown User") { }
                }
            })
        }
    }
    
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user
    
    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun saveUser(user: User) {
        _isLoading.value = true
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _isLoading.value = false
            _error.value = "No authenticated user found"
            Log.e("UserViewModel", "Attempting to save user without authentication")
            return
        }

        val authUserId = currentUser.uid
        Log.d("UserViewModel", "Saving user with Auth UID: $authUserId")
        
        repository.saveUser(user, authUserId)
            .addOnSuccessListener { userId ->
                _isLoading.value = false
                _userId.value = userId
                _user.value = user
                Log.d("UserViewModel", "User saved successfully with Auth UID: $userId")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
                Log.e("UserViewModel", "Failed to save user", e)
            }
    }

    fun getUser(userId: String) {
        _isLoading.value = true
        repository.getUser(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.value = false
                snapshot.getValue(User::class.java)?.let {
                    _user.value = it
                    _userId.value = snapshot.key
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _error.value = error.message
            }
        })
    }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    suspend fun updateUserAvatar(userId: String, bitmap: Bitmap): Resource<String> {
        return try {
            _isLoading.value = true
            val result = repository.updateUserAvatar(userId, bitmap)
            when (result) {
                is Resource.Success -> {
                    Log.d("UserViewModel", "Avatar uploaded successfully: ${result.data}")
                    result.data?.let { url ->
                        // Get current user data
                        val currentUser = _user.value
                        // Update user with new avatar URL
                        currentUser?.let { user ->
                            val updatedUser = user.copy(avatarUrl = url)
                            // Save updated user to Firebase
                            repository.saveUser(updatedUser, userId)
                                .addOnSuccessListener {
                                    _user.value = updatedUser
                                    Log.d("UserViewModel", "User updated with new avatar")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UserViewModel", "Failed to update user with new avatar", e)
                                }
                        }
                    }
                    result
                }
                is Resource.Error -> {
                    Log.e("UserViewModel", "Failed to upload avatar: ${result.message}")
                    result
                }
                else -> result
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating avatar", e)
            Resource.error("Failed to update avatar: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}