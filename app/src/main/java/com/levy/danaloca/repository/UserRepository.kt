package com.levy.danaloca.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.levy.danaloca.model.User

class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.reference.child("users")
    
    fun saveUser(user: User): Task<Void> {
        return usersRef.child(user.email.replace(".", ",")).setValue(user)
    }

    fun getUser(email: String): DatabaseReference {
        return usersRef.child(email.replace(".", ","))
    }

    fun getUserByPhone(phone: String): DatabaseReference {
        return usersRef.orderByChild("phone").equalTo(phone).ref
    }

    fun updateUser(email: String, updates: Map<String, Any>): Task<Void> {
        return usersRef.child(email.replace(".", ",")).updateChildren(updates)
    }

    fun deleteUser(email: String): Task<Void> {
        return usersRef.child(email.replace(".", ",")).removeValue()
    }
}