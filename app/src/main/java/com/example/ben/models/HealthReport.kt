package com.example.ben.models

data class HealthReport(
    val id: String = "",
    val beekeeperId: String = "",
    val hiveId: String = "", // e.g. "HIVE-01"
    val date: String = "",
    val colonyCondition: String = "", // Healthy, Weak, etc.
    val diseases: String = "",
    val notes: String = ""
)
