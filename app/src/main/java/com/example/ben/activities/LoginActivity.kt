package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityLoginBinding
import com.example.ben.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivityDebug"
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Login Page Opened")

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe user data for role-based navigation
        viewModel.userData.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "userData observer: User found, role: ${user.role}. Navigating...")
                val intent = when (user.role) {
                    "Farmer" -> Intent(this, FarmerDashboardActivity::class.java)
                    "Beekeeper" -> Intent(this, BeekeeperDashboardActivity::class.java)
                    else -> null
                }

                if (intent != null) {
                    Log.d(TAG, "userData observer: Starting Dashboard")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e(TAG, "userData observer: Role is invalid or missing: ${user.role}")
                    Toast.makeText(this, "Error: User role invalid.", Toast.LENGTH_LONG).show()
                    viewModel.logout() // Clear local state if role is broken
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Log.e(TAG, "error observer: Displaying error - $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            Log.d(TAG, "loading observer: Status - $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            
            // Safety: If somehow it stays visible, let's ensure buttons aren't permanently disabled
            // But with our finally blocks in ViewModel, this shouldn't be needed.
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Log.w(TAG, "setupClickListeners: Empty credentials")
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d(TAG, "setupClickListeners: Login button clicked for $email")
            viewModel.login(email, pass)
        }

        binding.tvSignup.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Signup link clicked")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Forgot Password clicked")
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
