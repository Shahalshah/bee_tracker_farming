package com.example.ben.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAlertBinding
import com.example.ben.models.Alert
import com.example.ben.models.User
import com.example.ben.utils.FirebaseUtils
import java.util.*

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private var userName: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        fetchUserName()

        binding.etSprayDate.setOnClickListener {
            showDatePicker()
        }

        binding.etSprayTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSendAlert.setOnClickListener {
            sendAlert()
        }
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                binding.etSprayDate.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                binding.etSprayTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun fetchUserName() {
        val uid = FirebaseUtils.currentUserUid ?: return
        FirebaseUtils.usersRef().child(uid).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            userName = user?.name ?: "Unknown Farmer"
        }
    }

    private fun sendAlert() {
        val date = binding.etSprayDate.text.toString().trim()
        val time = binding.etSprayTime.text.toString().trim()
        val pesticide = binding.etPesticide.text.toString().trim()

        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val alertId = FirebaseUtils.alertsRef().push().key ?: return
        val alert = Alert(
            id = alertId,
            farmerId = FirebaseUtils.currentUserUid ?: "",
            farmerName = userName,
            latitude = 12.9716, // Default for demo
            longitude = 77.5946,
            timestamp = System.currentTimeMillis(),
            message = "Spraying scheduled on $date at $time",
            date = date,
            time = time,
            pesticide = pesticide
        )

        FirebaseUtils.alertsRef().child(alertId).setValue(alert)
            .addOnSuccessListener {
                Toast.makeText(this, "Alert sent successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send alert: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
