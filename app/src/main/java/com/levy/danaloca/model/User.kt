package com.levy.danaloca.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val location: String = "",
    val birthdate: String = "",
    val age: String = "",
    val friends: Map<String, Boolean> = emptyMap(),
    val avatarUrl: String = "" // Cloudinary image URL
) {
    // Empty constructor required for Firebase
    constructor() : this(
        id = "",
        username = "",
        email = "",
        fullName = "",
        phoneNumber = "",
        gender = "",
        location = "",
        birthdate = "",
        age = "",
        friends = emptyMap(),
        avatarUrl = ""
    )

    fun isFriendWith(userId: String): Boolean {
        return friends[userId] == true
    }
}