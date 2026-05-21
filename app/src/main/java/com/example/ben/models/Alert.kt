package com.example.ben.models

data class Alert(
    val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pesticideName: String = "",
    val sprayDate: String = "",
    val sprayTime: String = "",
    val notes: String = "",
    val timestamp: Long = 0L,
    val message: String = ""
)
