package com.levy.danaloca.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.levy.danaloca.model.User

class UserRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun saveUser(user: User): Task<Void> {
        return database.child("users")
            .child(user.email.replace(".", "_"))
            .setValue(user)
    }

    fun updateUser(email: String, updates: Map<String, Any>): Task<Void> {
        return database.child("users")
            .child(email.replace(".", "_"))
            .updateChildren(updates)
    }

    fun getUser(email: String): DatabaseReference {
        return database.child("users")
            .child(email.replace(".", "_"))
    }

    fun getUserByPhone(phone: String): DatabaseReference {
        return database.child("users")
            .orderByChild("phoneNumber")
            .equalTo(phone)
            .ref
    }

    fun deleteUser(email: String): Task<Void> {
        return database.child("users")
            .child(email.replace(".", "_"))
            .removeValue()
    }
}