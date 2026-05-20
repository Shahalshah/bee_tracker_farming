package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityLoginBinding
import com.example.ben.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            viewModel.login(email, pass)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { authResult ->
            binding.progressBar.visibility = View.GONE
            if (authResult != null) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
