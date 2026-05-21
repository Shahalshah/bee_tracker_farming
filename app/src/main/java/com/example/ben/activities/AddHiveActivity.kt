package com.example.ben.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAddHiveBinding
import com.example.ben.models.Hive
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel

class AddHiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHiveBinding
    private val mainViewModel: MainViewModel by viewModels()
    
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleExtra("LAT", 0.0)
        longitude = intent.getDoubleExtra("LNG", 0.0)
        
        binding.etLocation.setText(String.format("%.6f, %.6f", latitude, longitude))

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupObservers()
        
        binding.btnSaveHive.setOnClickListener { validateAndSave() }
    }

    private fun setupObservers() {
        mainViewModel.status.observe(this) { status ->
            status?.let {
                if (it.contains("successfully", true)) {
                    Toast.makeText(this, "Hive Added Instantly", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
                mainViewModel.clearStatus()
            }
        }

        mainViewModel.loading.observe(this) { isLoading ->
            // Use a subtle loading state
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveHive.isEnabled = !isLoading
        }
    }

    private fun validateAndSave() {
        val uid = FirebaseUtils.currentUserUid
        if (uid == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etHiveName.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val population = binding.etPopulation.text.toString().trim()

        if (name.isEmpty()) {
            binding.etHiveName.error = "Name required"
            return
        }

        val hive = Hive(
            beekeeperId = uid,
            name = name,
            latitude = latitude,
            longitude = longitude,
            description = desc,
            population = population,
            status = "Active"
        )

        // The save is now instant due to Firebase Persistence and Coroutines
        mainViewModel.saveHive(hive)
    }
}
