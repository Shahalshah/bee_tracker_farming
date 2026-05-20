package com.example.ben.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAlertBinding
import com.example.ben.models.Alert
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AlertViewModel
import java.util.*

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private val viewModel: AlertViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.etSprayDate.setOnClickListener { showDatePicker() }
        binding.etSprayTime.setOnClickListener { showTimePicker() }

        binding.btnSendAlert.setOnClickListener { sendAlert() }

        viewModel.status.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (msg.contains("successfully")) finish()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            binding.etSprayDate.setText("$day/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(this, { _, hour, minute ->
            binding.etSprayTime.setText(String.format("%02d:%02d", hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun sendAlert() {
        val date = binding.etSprayDate.text.toString()
        val time = binding.etSprayTime.text.toString()
        val pesticide = binding.etPesticide.text.toString()

        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Select date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val alertId = FirebaseUtils.alertsRef().push().key ?: return
        val alert = Alert(
            id = alertId,
            farmerId = FirebaseUtils.currentUserUid ?: "",
            farmerName = "A Farmer", // This should be fetched from Profile
            latitude = 12.9716, // Should be actual GPS location
            longitude = 77.5946,
            sprayDate = date,
            sprayTime = time,
            pesticide = pesticide,
            timestamp = System.currentTimeMillis(),
            message = "Spraying scheduled on $date at $time"
        )
        viewModel.sendAlert(alert)
    }
}
