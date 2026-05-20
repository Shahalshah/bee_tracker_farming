package com.example.ben.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // "Farmer" or "Beekeeper"
    val fcmToken: String = ""
)
