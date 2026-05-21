package com.example.ben.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ben.R
import com.example.ben.databinding.ActivitySplashBinding
import com.example.ben.utils.FirebaseUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivityDebug"
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Splash screen started")

        // App logo entrance animation
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        binding.ivLogo.startAnimation(animation)
        binding.tvAppName.startAnimation(animation)

        // Use lifecycleScope for safe background navigation
        lifecycleScope.launch {
            // Mandatory splash delay for brand visibility
            delay(2500)
            checkAuthAndNavigate()
        }
    }

    private suspend fun checkAuthAndNavigate() {
        val currentUser = FirebaseUtils.auth.currentUser
        Log.d(TAG, "checkAuthAndNavigate: Checking Firebase Auth status...")

        if (currentUser == null) {
            Log.d(TAG, "checkAuthAndNavigate: No user logged in. Redirecting to Login.")
            navigateTo(LoginActivity::class.java)
            return
        }

        val uid = currentUser.uid
        Log.d(TAG, "checkAuthAndNavigate: User logged in with UID: $uid. Fetching role...")

        try {
            // Timeout protection: don't let the splash get stuck if the database is slow
            val role = withTimeoutOrNull(5000L) { // 5-second timeout
                Log.d(TAG, "checkAuthAndNavigate: Database fetch started")
                val snapshot = FirebaseUtils.usersRef().child(uid).child("role").get().await()
                val roleValue = snapshot.value as? String
                Log.d(TAG, "checkAuthAndNavigate: Role fetched successfully: $roleValue")
                roleValue
            }

            if (role == null) {
                Log.e(TAG, "checkAuthAndNavigate: FAILED to fetch role within timeout. Safety redirect to Login.")
                navigateTo(LoginActivity::class.java)
            } else {
                when (role) {
                    "Farmer" -> {
                        Log.d(TAG, "checkAuthAndNavigate: Navigating to Farmer Dashboard")
                        navigateTo(FarmerDashboardActivity::class.java)
                    }
                    "Beekeeper" -> {
                        Log.d(TAG, "checkAuthAndNavigate: Navigating to Beekeeper Dashboard")
                        navigateTo(BeekeeperDashboardActivity::class.java)
                    }
                    else -> {
                        Log.w(TAG, "checkAuthAndNavigate: Unknown role found: $role. Falling back to Login.")
                        navigateTo(LoginActivity::class.java)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAuthAndNavigate: ERROR during startup flow: ${e.message}", e)
            navigateTo(LoginActivity::class.java)
        }
    }

    private fun navigateTo(destination: Class<*>) {
        Log.d(TAG, "navigateTo: Triggering intent for ${destination.simpleName}")
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Log.d(TAG, "navigateTo: Splash screen FINISHED")
    }
}
