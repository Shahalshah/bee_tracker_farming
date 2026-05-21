package com.example.ben.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ben.databinding.ActivityAlertBinding
import com.example.ben.models.Alert
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class AlertActivity : AppCompatActivity() {

    private val TAG = "AlertActivityDebug"
    private lateinit var binding: ActivityAlertBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    
    private var farmerName = ""
    private var farmLat: Double = 0.0
    private var farmLng: Double = 0.0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission required to pinpoint alert location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupObservers()
        setupClickListeners()
        checkLocationPermission()
        
        FirebaseUtils.currentUserUid?.let { authViewModel.fetchUserData(it) }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                farmLat = location.latitude
                farmLng = location.longitude
                Log.d(TAG, "getCurrentLocation: Detected $farmLat, $farmLng")
            }
        }
    }

    private fun setupObservers() {
        authViewModel.userData.observe(this) { user ->
            user?.let { farmerName = it.name }
        }

        mainViewModel.status.observe(this) { status ->
            status?.let {
                Log.d(TAG, "mainViewModel.status observer: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (it.contains("successfully", true)) finish()
                mainViewModel.clearStatus()
            }
        }

        mainViewModel.loading.observe(this) { isLoading ->
            Log.d(TAG, "mainViewModel.loading observer: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSendAlert.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.etSprayDate.setOnClickListener { showDatePicker() }
        binding.etSprayTime.setOnClickListener { showTimePicker() }
        binding.btnSendAlert.setOnClickListener { validateAndSend() }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            binding.etSprayDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(this, { _, hour, minute ->
            binding.etSprayTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun validateAndSend() {
        val uid = FirebaseUtils.currentUserUid
        if (uid == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val pesticide = binding.etPesticide.text.toString().trim()
        val date = binding.etSprayDate.text.toString().trim()
        val time = binding.etSprayTime.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (pesticide.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill in pesticide, date, and time", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure we have a farmer name before sending, fallback to "Farmer"
        val displayName = if (farmerName.isBlank()) "Farmer" else farmerName

        Log.d(TAG, "validateAndSend: Preparing alert for $pesticide by $displayName")

        val alert = Alert(
            farmerId = uid,
            farmerName = displayName,
            pesticideName = pesticide,
            sprayDate = date,
            sprayTime = time,
            notes = notes,
            latitude = if (farmLat != 0.0) farmLat else 12.9716, // Fallback to Bangalore if GPS null
            longitude = if (farmLng != 0.0) farmLng else 77.5946,
            message = "Spray alert: $pesticide spraying scheduled on $date at $time",
            timestamp = System.currentTimeMillis()
        )

        mainViewModel.sendAlert(alert)
    }
}
