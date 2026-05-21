package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivitySignupBinding
import com.example.ben.models.User
import com.example.ben.viewmodels.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private val TAG = "SignupActivityDebug"
    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRoleSpinner()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Farmer", "Beekeeper")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.userData.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "userData observer: Signup successful for role ${user.role}")
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                val intent = if (user.role == "Farmer") {
                    Intent(this, FarmerDashboardActivity::class.java)
                } else {
                    Intent(this, BeekeeperDashboardActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
            binding.btnSignup.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "setupClickListeners: Attempting signup for $email as $role")
            val user = User(name = name, email = email, phone = phone, role = role)
            viewModel.signup(user, pass)
        }

        binding.tvLogin.setOnClickListener { finish() }
    }
}
