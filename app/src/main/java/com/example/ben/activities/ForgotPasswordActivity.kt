package com.example.ben.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityForgotPasswordBinding
import com.example.ben.viewmodels.AuthViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.forgotPassword(email)
        }

        viewModel.error.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (msg.contains("successfully")) finish()
        }
    }
}
