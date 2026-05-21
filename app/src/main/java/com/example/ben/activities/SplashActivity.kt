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

        // Startup sequence
        lifecycleScope.launch {
            Log.d(TAG, "onCreate: Startup delay started")
            delay(2000)
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
        Log.d(TAG, "checkAuthAndNavigate: User logged in (UID: $uid). Fetching role...")

        try {
            // Fetch role with timeout to prevent freezing
            val snapshot = withTimeoutOrNull(5000L) {
                Log.d(TAG, "checkAuthAndNavigate: DB Role fetch started")
                FirebaseUtils.usersRef().child(uid).child("role").get().await()
            }

            if (snapshot == null) {
                Log.e(TAG, "checkAuthAndNavigate: FAILED - DB fetch timeout (5s)")
                // If we can't get the role, we can't decide dashboard, so go to Login safely
                FirebaseUtils.auth.signOut()
                navigateTo(LoginActivity::class.java)
                return
            }

            val role = snapshot.value as? String
            Log.d(TAG, "checkAuthAndNavigate: Role fetched: $role")

            when (role) {
                "Farmer" -> navigateTo(FarmerDashboardActivity::class.java)
                "Beekeeper" -> navigateTo(BeekeeperDashboardActivity::class.java)
                else -> {
                    Log.e(TAG, "checkAuthAndNavigate: Unknown or missing role: $role. Signing out.")
                    FirebaseUtils.auth.signOut()
                    navigateTo(LoginActivity::class.java)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAuthAndNavigate: CRITICAL ERROR during startup: ${e.message}", e)
            navigateTo(LoginActivity::class.java)
        }
    }

    private fun navigateTo(destination: Class<*>) {
        Log.d(TAG, "navigateTo: Intent to ${destination.simpleName}")
        val intent = Intent(this, destination)
        // Clear stack to prevent back-navigation to splash
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Log.d(TAG, "navigateTo: Splash screen FINISHED and destroyed")
    }
}
