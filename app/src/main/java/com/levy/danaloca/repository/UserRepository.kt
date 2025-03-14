package com.levy.danaloca.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.levy.danaloca.model.User

class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.reference.child("users")
    
    fun saveUser(user: User, authUserId: String): Task<String> {
        // Use Firebase Auth UID as the database key
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

    fun updateUser(userId: String, updates: Map<String, Any>): Task<Void> {
        return usersRef.child(userId).updateChildren(updates)
    }

    fun deleteUser(userId: String): Task<Void> {
        return usersRef.child(userId).removeValue()
    }
}