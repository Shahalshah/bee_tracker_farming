package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityProfileBinding
import com.example.ben.models.User
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        fetchUserProfile()

        binding.btnLogout.setOnClickListener {
            FirebaseUtils.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserProfile() {
        val uid = FirebaseUtils.currentUserUid ?: return
        FirebaseUtils.usersRef().child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.tvProfileName.text = it.name
                    binding.tvProfileRole.text = it.role
                    binding.tvProfileEmail.text = it.email
                    binding.tvProfilePhone.text = it.phone
                    binding.tvProfileLocation.text = "Bangalore, Karnataka" // Placeholder
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
