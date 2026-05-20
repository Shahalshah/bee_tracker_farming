package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ben.models.User
import com.example.ben.repositories.AuthRepository
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.auth.AuthResult

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthResult?>()
    val authState: LiveData<AuthResult?> = _authState

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun login(email: String, pass: String) {
        repository.login(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authState.value = task.result
            } else {
                _error.value = task.exception?.message
            }
        }
    }

    fun signup(user: User, pass: String) {
        repository.signup(user.email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newUser = user.copy(uid = task.result?.user?.uid ?: "")
                saveUser(newUser)
            } else {
                _error.value = task.exception?.message
            }
        }
    }

    private fun saveUser(user: User) {
        repository.saveUser(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userData.value = user
            } else {
                _error.value = task.exception?.message
            }
        }
    }

    fun fetchUserData(uid: String) {
        repository.getUserData(uid).addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            _userData.value = user
        }.addOnFailureListener {
            _error.value = it.message ?: "An unknown error occurred"
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = null
        _userData.value = null
    }

    fun forgotPassword(email: String) {
        repository.forgotPassword(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _error.value = "Reset email sent successfully!"
            } else {
                _error.value = task.exception?.message
            }
        }
    }
}
