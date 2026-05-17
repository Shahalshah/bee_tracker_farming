package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.R
import com.example.ben.databinding.ActivityDashboardBinding
import com.example.ben.models.User
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private var userRole: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        fetchUserProfile()

        binding.cardAction1.setOnClickListener {
            if (userRole == "Farmer") {
                startActivity(Intent(this, AlertActivity::class.java))
            } else {
                // Beekeeper: Add Hive Location
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("ACTION", "ADD_HIVE")
                startActivity(intent)
            }
        }

        binding.cardAction2.setOnClickListener {
            if (userRole == "Farmer") {
                startActivity(Intent(this, MapActivity::class.java))
            } else {
                startActivity(Intent(this, HealthTrackerActivity::class.java))
            }
        }

        binding.cardAction3.setOnClickListener {
            if (userRole == "Farmer") {
                startActivity(Intent(this, TipsActivity::class.java))
            } else {
                startActivity(Intent(this, HoneyProductionActivity::class.java))
            }
        }

        binding.cardAction4.setOnClickListener {
            startActivity(Intent(this, NotificationHistoryActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    false
                }
                R.id.nav_health -> {
                    if (userRole == "Beekeeper") {
                        startActivity(Intent(this, HealthTrackerActivity::class.java))
                    } else {
                        Toast.makeText(this, "Health Tracker (Beekeeper only)", Toast.LENGTH_SHORT).show()
                    }
                    false
                }
                R.id.nav_tips -> {
                    if (userRole == "Farmer") {
                        startActivity(Intent(this, TipsActivity::class.java))
                    } else {
                        startActivity(Intent(this, NotificationHistoryActivity::class.java))
                    }
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun fetchUserProfile() {
        val uid = FirebaseUtils.currentUserUid ?: return
        FirebaseUtils.usersRef().child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    userRole = it.role
                    if (userRole == "Beekeeper") {
                        binding.tvWelcome.text = getString(R.string.hello_beekeeper)
                    } else {
                        binding.tvWelcome.text = getString(R.string.hello_user, it.name)
                    }
                    updateUIBasedOnRole()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUIBasedOnRole() {
        if (userRole == "Beekeeper") {
            binding.tvBannerText.text = getString(R.string.beekeeper_banner)
            binding.tvAction1.text = getString(R.string.add_hive_location)
            binding.tvAction1Sub.text = ""
            binding.ivAction1.setImageResource(android.R.drawable.ic_menu_add)

            binding.tvAction2.text = getString(R.string.health_tracker)
            binding.tvAction2Sub.text = ""
            binding.ivAction2.setImageResource(android.R.drawable.ic_menu_edit)

            binding.tvAction3.text = getString(R.string.honey_production)
            binding.tvAction3Sub.text = ""
            binding.ivAction3.setImageResource(android.R.drawable.ic_menu_save)

            binding.tvAction4.text = getString(R.string.history_notification)
            binding.tvAction4Sub.text = ""
        } else {
            binding.tvBannerText.text = getString(R.string.farmer_banner)
            binding.tvAction1.text = getString(R.string.spray_alert)
            binding.tvAction1Sub.text = getString(R.string.spraying_today_sub)
            binding.ivAction1.setImageResource(android.R.drawable.ic_dialog_alert)

            binding.tvAction2.text = getString(R.string.hive_map)
            binding.tvAction2Sub.text = getString(R.string.nearby_hives_sub)
            binding.ivAction2.setImageResource(android.R.drawable.ic_dialog_map)

            binding.tvAction3.text = getString(R.string.bee_tips)
            binding.tvAction3Sub.text = getString(R.string.learn_protect)
            binding.ivAction3.setImageResource(android.R.drawable.ic_menu_info_details)

            binding.tvAction4.text = getString(R.string.notification_history)
            binding.tvAction4Sub.text = getString(R.string.view_alerts)
        }
    }
}
