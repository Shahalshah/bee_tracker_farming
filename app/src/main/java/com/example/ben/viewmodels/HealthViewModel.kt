package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ben.models.HealthReport
import com.example.ben.repositories.DataRepository
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HealthViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _reports = MutableLiveData<List<HealthReport>>()
    val reports: LiveData<List<HealthReport>> = _reports

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun fetchReports() {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.getHealthReports(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HealthReport>()
                for (shot in snapshot.children) {
                    shot.getValue(HealthReport::class.java)?.let { list.add(0, it) }
                }
                _reports.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                _status.value = "Error: ${error.message}"
            }
        })
    }

    fun saveReport(report: HealthReport) {
        repository.saveHealthReport(report).addOnCompleteListener {
            if (it.isSuccessful) _status.value = "Report saved successfully"
            else _status.value = "Failed: ${it.exception?.message ?: "Unknown error"}"
        }
    }

    fun deleteReport(reportId: String) {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.deleteHealthReport(uid, reportId).addOnCompleteListener {
            if (it.isSuccessful) _status.value = "Report deleted"
            else _status.value = "Failed to delete"
        }
    }
}
