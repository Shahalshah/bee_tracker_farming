package com.example.ben.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ben.models.*
import com.example.ben.repositories.DataRepository
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val repository = DataRepository()

    private val _hives = MutableLiveData<List<Hive>>()
    val hives: LiveData<List<Hive>> = _hives

    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>> = _alerts

    private val _healthReports = MutableLiveData<List<HealthReport>>()
    val healthReports: LiveData<List<HealthReport>> = _healthReports

    private val _honeyRecords = MutableLiveData<List<HoneyRecord>>()
    val honeyRecords: LiveData<List<HoneyRecord>> = _honeyRecords

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> = _status

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Hives - Realtime Listener (Efficient)
    fun fetchAllHives() {
        // We use a listener so we don't need to call it repeatedly
        repository.getAllHives().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Hive>()
                for (shot in snapshot.children) {
                    shot.getValue(Hive::class.java)?.let { list.add(it) }
                }
                _hives.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                _status.value = "Map Error: ${error.message}"
            }
        })
    }

    // Optimized Save using Coroutines
    fun saveHive(hive: Hive) {
        if (hive.name.isEmpty()) {
            _status.value = "Hive name cannot be empty"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.saveHive(hive).await()
                }
                _status.value = "Hive saved successfully!"
            } catch (e: Exception) {
                _status.value = "Save Failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteHive(hiveId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteHive(hiveId).await()
                }
                _status.value = "Hive deleted"
            } catch (e: Exception) {
                _status.value = "Delete Failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Alerts
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
                _status.value = "Alerts Error: ${error.message}"
            }
        })
    }

    fun sendAlert(alert: Alert) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.sendAlert(alert).await()
                }
                _status.value = "Alert sent successfully"
            } catch (e: Exception) {
                _status.value = "Failed to send alert"
            } finally {
                _loading.value = false
            }
        }
    }

    // Health
    fun fetchHealthReports() {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.getHealthReports(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HealthReport>()
                for (shot in snapshot.children) {
                    shot.getValue(HealthReport::class.java)?.let { list.add(0, it) }
                }
                _healthReports.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                _status.value = error.message
            }
        })
    }

    fun saveHealthReport(report: HealthReport) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.saveHealthReport(report).await()
                }
                _status.value = "Health report saved"
            } catch (e: Exception) {
                _status.value = "Failed to save report"
            } finally {
                _loading.value = false
            }
        }
    }

    // Honey
    fun fetchHoneyRecords() {
        val uid = FirebaseUtils.currentUserUid ?: return
        repository.getHoneyRecords(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<HoneyRecord>()
                for (shot in snapshot.children) {
                    shot.getValue(HoneyRecord::class.java)?.let { list.add(0, it) }
                }
                _honeyRecords.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                _status.value = error.message
            }
        })
    }

    fun saveHoneyRecord(record: HoneyRecord) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.saveHoneyRecord(record).await()
                }
                _status.value = "Honey record saved"
            } catch (e: Exception) {
                _status.value = "Failed to save record"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
