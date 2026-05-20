package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ben.models.Alert
import com.example.ben.repositories.DataRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlertViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>> = _alerts

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun fetchAlerts() {
        repository.getAlerts().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Alert>()
                for (shot in snapshot.children) {
                    shot.getValue(Alert::class.java)?.let { list.add(0, it) }
                }
                _alerts.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                _status.value = "Error: ${error.message}"
            }
        })
    }

    fun sendAlert(alert: Alert) {
        repository.sendAlert(alert).addOnCompleteListener {
            if (it.isSuccessful) _status.value = "Alert sent successfully"
            else _status.value = "Failed to send alert"
        }
    }
}
