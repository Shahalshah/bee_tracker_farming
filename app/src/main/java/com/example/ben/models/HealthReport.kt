package com.example.ben.models

data class HealthReport(
    val id: String = "",
    val beekeeperId: String = "",
    val hiveId: String = "",
    val date: String = "",
    val colonyCondition: String = "",
    val diseases: String = "",
    val population: String = "",
    val notes: String = "",
    val timestamp: Long = 0L
)
