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

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe user data for role-based navigation
        viewModel.userData.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "userData observer: Redirecting user with role ${user.role}")
                val intent = if (user.role == "Farmer") {
                    Intent(this, FarmerDashboardActivity::class.java)
                } else {
                    Intent(this, BeekeeperDashboardActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Log.e(TAG, "error observer: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            Log.d(TAG, "loading observer: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d(TAG, "Login button clicked for $email")
            viewModel.login(email, pass)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
