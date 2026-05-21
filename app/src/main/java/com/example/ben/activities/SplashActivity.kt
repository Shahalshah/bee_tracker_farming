package com.example.ben.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.R
import com.example.ben.databinding.ActivitySplashBinding
import com.example.ben.utils.FirebaseUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        binding.ivLogo.startAnimation(animation)
        binding.tvAppName.startAnimation(animation)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuth()
        }, 2500)
    }

    private fun checkAuth() {
        val uid = FirebaseUtils.currentUserUid
        if (uid != null) {
            FirebaseUtils.usersRef().child(uid).child("role").get().addOnSuccessListener {
                val role = it.value as? String
                if (role == "Farmer") {
                    startActivity(Intent(this, FarmerDashboardActivity::class.java))
                } else if (role == "Beekeeper") {
                    startActivity(Intent(this, BeekeeperDashboardActivity::class.java))
                } else {
                    // Fallback or error
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }.addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
