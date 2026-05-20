package com.example.ben.repositories

import com.example.ben.models.User
import com.example.ben.utils.FirebaseUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot

class AuthRepository {
    private val auth = FirebaseUtils.auth
    private val db = FirebaseUtils.usersRef()

    fun login(email: String, pass: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, pass)
    }

    fun signup(email: String, pass: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, pass)
    }

    fun saveUser(user: User): Task<Void> {
        return db.child(user.uid).setValue(user)
    }

    fun getUserData(uid: String): Task<DataSnapshot> {
        return db.child(uid).get()
    }

    fun logout() {
        auth.signOut()
    }

    fun forgotPassword(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }
}
