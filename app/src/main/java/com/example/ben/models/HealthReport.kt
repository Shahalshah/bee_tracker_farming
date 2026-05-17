package com.example.ben.models

data class HealthReport(
    val id: String = "",
    val hiveId: String = "",
    val date: String = "",
    val honeyProduced: Double = 0.0,
    val healthStatus: String = "", // e.g., Healthy, Infected, Weak
    val notes: String = ""
)
