package com.example.ben.models

data class HoneyRecord(
    val id: String = "",
    val beekeeperId: String = "",
    val harvestDate: String = "",
    val quantity: Double = 0.0,
    val quality: String = "",
    val notes: String = "",
    val timestamp: Long = 0L
)
