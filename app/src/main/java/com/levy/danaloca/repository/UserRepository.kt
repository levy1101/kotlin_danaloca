package com.levy.danaloca.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.levy.danaloca.model.User
import com.levy.danaloca.utils.Resource

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