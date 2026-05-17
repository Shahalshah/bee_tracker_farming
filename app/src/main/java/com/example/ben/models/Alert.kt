package com.example.ben.models

data class Alert(
    val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val message: String = "",
    val date: String = "",
    val time: String = "",
    val pesticide: String = ""
)
