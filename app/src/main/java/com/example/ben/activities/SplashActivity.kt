package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.R
import com.example.ben.utils.FirebaseUtils

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (FirebaseUtils.auth.currentUser != null) {
                // User is signed in, go to Dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // No user is signed in, go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000) // 2 seconds delay
    }
}
