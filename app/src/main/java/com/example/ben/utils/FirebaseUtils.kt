package com.example.ben.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    
    val currentUserUid: String?
        get() = auth.currentUser?.uid

    fun usersRef() = database.getReference("users")
    fun hivesRef() = database.getReference("hives")
    fun alertsRef() = database.getReference("alerts")
    fun healthReportsRef() = database.getReference("health_reports")
}
