package com.example.ben.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Hive(
    val id: String = "",
    val beekeeperId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Active",
    val description: String = "",
    val population: String = "",
    val colonyCondition: String = "",
    val timestamp: Long = 0L
)
