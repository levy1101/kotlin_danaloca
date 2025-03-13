package com.levy.danaloca.model

data class User(
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val gender: String,
    val location: String,
    val birthdate: String,
    val age: String
)