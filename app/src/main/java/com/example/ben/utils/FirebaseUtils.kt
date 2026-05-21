package com.example.ben.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseUtils {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    
    // Explicitly handle database instance to avoid hanging and URL issues
    val database: FirebaseDatabase by lazy {
        val db = try {
            FirebaseDatabase.getInstance() 
        } catch (e: Exception) {
            FirebaseDatabase.getInstance("https://beee-1db3f-default-rtdb.firebaseio.com/")
        }
        // ENABLE persistence for instant local updates and offline support
        db.setPersistenceEnabled(true)
        db
    }
    
    val currentUserUid: String?
        get() = auth.currentUser?.uid

    fun usersRef() = database.getReference("users")
    fun hivesRef() = database.getReference("hives")
    fun alertsRef() = database.getReference("alerts")
    fun healthReportsRef() = database.getReference("health_reports")
    fun honeyProductionRef() = database.getReference("honey_production")
}
