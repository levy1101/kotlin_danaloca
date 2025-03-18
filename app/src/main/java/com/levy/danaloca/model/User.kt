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
    val avatar: String = "" // Base64 encoded image string
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
        avatar = ""
    )

    fun isFriendWith(userId: String): Boolean {
        return friends[userId] == true
    }
}