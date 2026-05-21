package com.example.ben.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAlertBinding
import com.example.ben.models.Alert
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel
import java.util.*

class AlertActivity : AppCompatActivity() {

    private val TAG = "AlertActivityDebug"
    private lateinit var binding: ActivityAlertBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    
    private var farmerName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupObservers()
        setupClickListeners()
        
        FirebaseUtils.currentUserUid?.let { authViewModel.fetchUserData(it) }
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
        val pesticide = binding.etPesticide.text.toString().trim()
        val date = binding.etSprayDate.text.toString().trim()
        val time = binding.etSprayTime.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (pesticide.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill in pesticide, date, and time", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "validateAndSend: Preparing alert for $pesticide")

        val alert = Alert(
            farmerId = FirebaseUtils.currentUserUid ?: "",
            farmerName = if (farmerName.isEmpty()) "A Farmer" else farmerName,
            pesticideName = pesticide,
            sprayDate = date,
            sprayTime = time,
            notes = notes,
            latitude = 12.9716, // Default; in production, use FusedLocationProvider
            longitude = 77.5946,
            message = "Spray alert: $pesticide spraying scheduled on $date at $time"
        )

        mainViewModel.sendAlert(alert)
    }
}
