package com.example.ben.viewmodels

import android.util.Log
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
import kotlinx.coroutines.withTimeout

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModelDebug"
    private val repository = DataRepository()

    private val _hives = MutableLiveData<List<Hive>>()
    val hives: LiveData<List<Hive>> = _hives

    private val _alerts = MutableLiveData<List<Alert>>()
    val alerts: LiveData<List<Alert>> = _alerts

    private val _healthReports = MutableLiveData<List<HealthReport>>()
    val healthReports: LiveData<List<HealthReport>> = _healthReports

    private val _honeyRecords = MutableLiveData<List<HoneyRecord>>()
    val honeyRecords: LiveData<List<HoneyRecord>> = _honeyRecords

    // Beekeeper Statistics
    private val _hiveCount = MutableLiveData<Int>()
    val hiveCount: LiveData<Int> = _hiveCount

    private val _totalHoney = MutableLiveData<Double>()
    val totalHoney: LiveData<Double> = _totalHoney

    private val _activeAlertsCount = MutableLiveData<Int>()
    val activeAlertsCount: LiveData<Int> = _activeAlertsCount

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> = _status

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Hives - Realtime Listener (Efficient)
    fun fetchAllHives() {
        val uid = FirebaseUtils.currentUserUid ?: return
        Log.d(TAG, "fetchAllHives: Started for user: $uid")
        
        repository.getAllHives().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Hive>()
                for (shot in snapshot.children) {
                    shot.getValue(Hive::class.java)?.let { list.add(it) }
                }
                _hives.value = list
                
                // Calculate beekeeper specific hive count
                val beekeeperHives = list.filter { it.beekeeperId == uid }
                _hiveCount.value = beekeeperHives.size
                Log.d(TAG, "fetchAllHives: Updated hiveCount to ${_hiveCount.value}")
            }
            override fun onCancelled(error: DatabaseError) {
                _status.value = "Map Error: ${error.message}"
            }
        })
    }

    // Optimized Save using Coroutines with Timeout and Detailed Logging
    fun saveHive(hive: Hive) {
        val uid = FirebaseUtils.currentUserUid
        Log.d(TAG, "saveHive: Started for user: $uid hiveName: ${hive.name} lat: ${hive.latitude} lng: ${hive.longitude}")

        if (uid == null) {
            _status.value = "User not logged in. Please log in to save location."
            return
        }

        if (hive.name.isEmpty()) {
            _status.value = "Hive name cannot be empty"
            return
        }

        if (hive.latitude == 0.0 || hive.longitude == 0.0) {
            _status.value = "Invalid coordinates. Please select a location on the map."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            Log.d(TAG, "saveHive: Loading shown. Attempting Firebase write...")
            try {
                withTimeout(7000L) { // Increased to 7 seconds for slower networks
                    withContext(Dispatchers.IO) {
                        repository.saveHive(hive).await()
                    }
                    Log.d(TAG, "saveHive: Firebase write SUCCESS")
                }
                _status.value = "Hive location saved successfully!"
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "saveHive: FAILED due to Timeout (7s)")
                _status.value = "Network timeout. Check your internet connection."
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.e(TAG, "saveHive: FAILED with exception: $errorMsg", e)
                if (errorMsg.contains("permission", true) || errorMsg.contains("denied", true)) {
                    _status.value = "Permission Denied! Ensure database rules allow authenticated writes."
                } else {
                    _status.value = "Failed to save hive: $errorMsg"
                }
            } finally {
                _loading.value = false
                Log.d(TAG, "saveHive: Loading hidden")
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
                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000
                var activeCount = 0
                
                for (shot in snapshot.children) {
                    shot.getValue(Alert::class.java)?.let { 
                        list.add(0, it)
                        if (now - it.timestamp < oneDayMillis) {
                            activeCount++
                        }
                    }
                }
                _alerts.value = list
                _activeAlertsCount.value = activeCount
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
                var total = 0.0
                for (shot in snapshot.children) {
                    shot.getValue(HoneyRecord::class.java)?.let { 
                        list.add(0, it)
                        total += it.quantity
                    }
                }
                _honeyRecords.value = list
                _totalHoney.value = total
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
