package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivitySignupBinding
import com.example.ben.models.User
import com.example.ben.viewmodels.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleSpinner()
        observeViewModel()

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

            val user = User(name = name, email = email, phone = phone, role = role)
            binding.progressBar.visibility = View.VISIBLE
            viewModel.signup(user, pass)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Farmer", "Beekeeper")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.userData.observe(this) { user ->
            binding.progressBar.visibility = View.GONE
            if (user != null) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java))
                finishAffinity()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
