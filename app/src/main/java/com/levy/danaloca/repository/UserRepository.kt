package com.levy.danaloca.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource
import com.levy.danaloca.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository(private val context: Context) {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.reference.child("users")
    
    fun saveUser(user: User, authUserId: String): Task<String> {
        return usersRef.child(authUserId).setValue(user).continueWith { task ->
            if (task.isSuccessful) {
                authUserId
            } else {
                throw task.exception ?: Exception("Failed to save user")
            }
        }
    }

    fun getUser(userId: String): DatabaseReference {
        return usersRef.child(userId)
    }

    fun getUserByEmail(email: String): DatabaseReference {
        return usersRef.orderByChild("email").equalTo(email).ref
    }

    fun getUserByPhone(phone: String): DatabaseReference {
        return usersRef.orderByChild("phoneNumber").equalTo(phone).ref
    }

    suspend fun updateUserAvatar(userId: String, bitmap: Bitmap): Resource<String> {
        return try {
            withContext(Dispatchers.IO) {
                val imageUrl = ImageUtils.uploadImage(context, bitmap, "avatar")
                suspendCoroutine { continuation ->
                    usersRef.child(userId).child("avatarUrl").setValue(imageUrl)
                        .addOnSuccessListener {
                            continuation.resume(Resource.success(imageUrl))
                        }
                        .addOnFailureListener { e ->
                            continuation.resume(Resource.error("Failed to update avatar: ${e.message}"))
                        }
                }
            }
        } catch (e: Exception) {
            Resource.error("Failed to update avatar: ${e.message}")
        }
    }

    fun updateUser(userId: String, updates: Map<String, Any>): Task<Void> {
        return usersRef.child(userId).updateChildren(updates)
    }

    fun deleteUser(userId: String): Task<Void> {
        return usersRef.child(userId).removeValue()
    }

    fun getUserProfile(userId: String, callback: (Resource<User>) -> Unit) {
        callback(Resource.loading())

        usersRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    callback(Resource.success(user.copy(id = snapshot.key ?: "")))
                } else {
                    callback(Resource.error("User not found"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Resource.error(error.message))
            }
        })
    }
}