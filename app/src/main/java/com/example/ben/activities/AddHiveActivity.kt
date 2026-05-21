package com.example.ben.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAddHiveBinding
import com.example.ben.models.Hive
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel

class AddHiveActivity : AppCompatActivity() {

    private val TAG = "AddHiveActivityDebug"
    private lateinit var binding: ActivityAddHiveBinding
    private val mainViewModel: MainViewModel by viewModels()
    
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: Add Hive Details Page Opened")

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
                Log.d(TAG, "setupObservers: status change - $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it.contains("successfully", true)) {
                    Log.d(TAG, "setupObservers: Closing activity on success")
                    finish()
                }
                mainViewModel.clearStatus()
            }
        }

        mainViewModel.loading.observe(this) { isLoading ->
            Log.d(TAG, "setupObservers: loading status - $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveHive.isEnabled = !isLoading
        }
    }

    private fun validateAndSave() {
        Log.d(TAG, "validateAndSave: Save Button Clicked")
        val uid = FirebaseUtils.currentUserUid
        if (uid == null) {
            Log.e(TAG, "validateAndSave: FAILED - No User UID")
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val name = binding.etHiveName.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val population = binding.etPopulation.text.toString().trim()

        if (name.isEmpty()) {
            Log.w(TAG, "validateAndSave: FAILED - Name is empty")
            binding.etHiveName.error = "Name required"
            return
        }

        if (latitude == 0.0 || longitude == 0.0) {
            Log.e(TAG, "validateAndSave: FAILED - Coordinates are 0.0")
            Toast.makeText(this, "Error: Location coordinates invalid.", Toast.LENGTH_SHORT).show()
            return
        }

        val hive = Hive(
            beekeeperId = uid,
            name = name,
            latitude = latitude,
            longitude = longitude,
            description = desc,
            population = population,
            status = "Active",
            timestamp = System.currentTimeMillis()
        )

        Log.i(TAG, "validateAndSave: Sending Hive object to ViewModel for Firebase Write: $hive")
        mainViewModel.saveHive(hive)
    }
}
