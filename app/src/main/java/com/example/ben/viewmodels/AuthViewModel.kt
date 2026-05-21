package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ben.models.User
import com.example.ben.repositories.AuthRepository
import com.google.firebase.auth.AuthResult

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthResult?>()
    val authState: LiveData<AuthResult?> = _authState

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun login(email: String, pass: String) {
        _loading.value = true
        repository.login(email, pass).addOnCompleteListener { task ->
            _loading.value = false
            if (task.isSuccessful) {
                _authState.value = task.result
                fetchUserData(task.result?.user?.uid ?: "")
            } else {
                _error.value = task.exception?.message ?: "Login failed"
            }
        }
    }

    fun signup(user: User, pass: String) {
        _loading.value = true
        repository.signup(user.email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newUser = user.copy(uid = task.result?.user?.uid ?: "")
                saveUser(newUser)
            } else {
                _loading.value = false
                _error.value = task.exception?.message ?: "Signup failed"
            }
        }
    }

    private fun saveUser(user: User) {
        repository.saveUser(user).addOnCompleteListener { task ->
            _loading.value = false
            if (task.isSuccessful) {
                _userData.value = user
            } else {
                _error.value = task.exception?.message ?: "Failed to save user data"
            }
        }
    }

    fun fetchUserData(uid: String) {
        _loading.value = true
        repository.getUserData(uid).addOnSuccessListener { snapshot ->
            _loading.value = false
            val user = snapshot.getValue(User::class.java)
            _userData.value = user
        }.addOnFailureListener {
            _loading.value = false
            _error.value = it.message ?: "Failed to fetch user data"
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = null
        _userData.value = null
    }

    fun forgotPassword(email: String) {
        _loading.value = true
        repository.forgotPassword(email).addOnCompleteListener { task ->
            _loading.value = false
            if (task.isSuccessful) {
                _error.value = "Reset email sent successfully!"
            } else {
                _error.value = task.exception?.message ?: "Failed to send reset email"
            }
        }
    }

    fun updateFcmToken(token: String) {
        val uid = _userData.value?.uid ?: return
        repository.updateFcmToken(uid, token)
    }

    fun clearError() {
        _error.value = null
    }
}
