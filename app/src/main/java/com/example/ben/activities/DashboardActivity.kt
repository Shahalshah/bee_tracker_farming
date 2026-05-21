package com.example.ben.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ben.R
import com.example.ben.databinding.ActivityDashboardBinding
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        fetchData()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun fetchData() {
        val uid = FirebaseUtils.currentUserUid
        if (uid != null) {
            authViewModel.fetchUserData(uid)
            mainViewModel.fetchAlerts()
            mainViewModel.fetchAllHives()
        } else {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
