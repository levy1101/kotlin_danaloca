package com.levy.danaloca.model

data class User(
    val email: String = "",      // Default values for all properties
    val fullName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val location: String = "",
    val birthdate: String = "",
    val age: String = ""
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", "", "", "", "")
}