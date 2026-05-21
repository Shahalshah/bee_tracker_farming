package com.example.ben.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ben.models.User
import com.example.ben.repositories.AuthRepository
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModelDebug"
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
        Log.d(TAG, "login: Started for $email")
        viewModelScope.launch {
            _loading.value = true
            try {
                // 5-second timeout for authentication
                val result = withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.login(email, pass).await()
                    }
                }
                Log.d(TAG, "login: Auth success, fetching user data...")
                _authState.value = result
                fetchUserData(result.user?.uid ?: "")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "login: Timeout")
                _error.value = "Login timed out. Please check your internet."
                _loading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "login: Failed - ${e.message}")
                _error.value = e.message ?: "Login failed"
                _loading.value = false
            }
        }
    }

    fun signup(user: User, pass: String) {
        Log.d(TAG, "signup: Started for ${user.email}")
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = withTimeout(7000L) {
                    withContext(Dispatchers.IO) {
                        repository.signup(user.email, pass).await()
                    }
                }
                val newUser = user.copy(uid = result.user?.uid ?: "")
                saveUser(newUser)
            } catch (e: Exception) {
                Log.e(TAG, "signup: Failed - ${e.message}")
                _error.value = e.message ?: "Signup failed"
                _loading.value = false
            }
        }
    }

    private fun saveUser(user: User) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.saveUser(user).await()
                }
                Log.d(TAG, "saveUser: Success")
                _userData.value = user
            } catch (e: Exception) {
                Log.e(TAG, "saveUser: Failed - ${e.message}")
                _error.value = "Failed to save user data"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchUserData(uid: String) {
        if (uid.isEmpty()) return
        Log.d(TAG, "fetchUserData: Started for $uid")
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.getUserData(uid).await()
                    }
                }
                val user = snapshot.getValue(User::class.java)
                Log.d(TAG, "fetchUserData: Success, role: ${user?.role}")
                _userData.value = user
            } catch (e: Exception) {
                Log.e(TAG, "fetchUserData: Failed - ${e.message}")
                _error.value = "Failed to fetch user role"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = null
        _userData.value = null
    }

    fun forgotPassword(email: String) {
        Log.d(TAG, "forgotPassword: Started for $email")
        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.forgotPassword(email).await()
                }
                _error.value = "Reset email sent successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "forgotPassword: Failed - ${e.message}")
                _error.value = e.message ?: "Failed to send reset email"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
