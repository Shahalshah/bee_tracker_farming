package com.example.ben.models

data class Hive(
    val id: String = "",
    val beekeeperId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Active", // Active, Inactive
    val description: String = "",
    val population: String = "",
    val colonyCondition: String = ""
)
