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

    // Statistics
    private val _hiveCount = MutableLiveData<Int>()
    val hiveCount: LiveData<Int> = _hiveCount

    private val _totalHoney = MutableLiveData<Double>()
    val totalHoney: LiveData<Double> = _totalHoney

    private val _activeAlertsCount = MutableLiveData<Int>()
    val activeAlertsCount: LiveData<Int> = _activeAlertsCount

    private val _alertsSentCount = MutableLiveData<Int>()
    val alertsSentCount: LiveData<Int> = _alertsSentCount

    private val _nearbyHivesCount = MutableLiveData<Int>()
    val nearbyHivesCount: LiveData<Int> = _nearbyHivesCount

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> = _status

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // Hives - Responsive with Real-time Listener
    fun fetchAllHives() {
        val uid = FirebaseUtils.currentUserUid ?: return
        Log.d(TAG, "fetchAllHives: Started (Real-time)")
        
        repository.getAllHives().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Hive>()
                for (shot in snapshot.children) {
                    shot.getValue(Hive::class.java)?.let { list.add(it) }
                }
                _hives.value = list
                
                // Stats
                _hiveCount.value = list.count { it.beekeeperId == uid }
                _nearbyHivesCount.value = list.size
                Log.d(TAG, "fetchAllHives: Success, total: ${list.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchAllHives: Failed - ${error.message}")
                _status.value = "Failed to sync map data"
            }
        })
    }

    fun saveHive(hive: Hive) {
        val uid = FirebaseUtils.currentUserUid
        Log.d(TAG, "saveHive: Started for ${hive.name}")

        if (uid == null) {
            _status.value = "Session expired"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(2000L) {
                    withContext(Dispatchers.IO) {
                        repository.saveHive(hive).await()
                    }
                    _status.value = "Hive location saved successfully!"
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.d(TAG, "saveHive: Network slow, hive saved locally.")
                _status.value = "Hive saved locally. Syncing..."
            } catch (e: Exception) {
                Log.e(TAG, "saveHive: Failed - ${e.message}")
                _status.value = "Failed to save hive: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Alerts - Responsive with Real-time Listener
    fun fetchAlerts() {
        val uid = FirebaseUtils.currentUserUid ?: return
        Log.d(TAG, "fetchAlerts: Started (Real-time)")
        
        // Remove existing listener if any to avoid duplicates
        // (Simple implementation: just add the listener)
        repository.getAlerts().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Alert>()
                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000
                var activeCount = 0
                var sentByMe = 0

                for (shot in snapshot.children) {
                    shot.getValue(Alert::class.java)?.let { 
                        list.add(0, it)
                        if (now - it.timestamp < oneDayMillis) activeCount++
                        if (it.farmerId == uid) sentByMe++
                    }
                }
                _alerts.value = list
                _activeAlertsCount.value = activeCount
                _alertsSentCount.value = sentByMe
                Log.d(TAG, "fetchAlerts: Success, total: ${list.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchAlerts: Failed - ${error.message}")
                _status.value = "Failed to sync alerts"
            }
        })
    }

    fun sendAlert(alert: Alert) {
        val uid = FirebaseUtils.currentUserUid
        Log.d(TAG, "sendAlert: Started for pesticide: ${alert.pesticideName}")

        if (uid == null) {
            _status.value = "Session expired. Please login again."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            Log.d(TAG, "sendAlert: Loading shown")
            try {
                // To make it feel "Instant", we perform the write and don't wait for server confirmation 
                // if it takes more than 2 seconds. Persistence will handle the rest.
                withTimeout(2000L) { 
                    Log.d(TAG, "sendAlert: Firebase write started")
                    withContext(Dispatchers.IO) {
                        repository.sendAlert(alert).await()
                    }
                    Log.d(TAG, "sendAlert: Firebase write success (Server)")
                    _status.value = "Alert sent successfully!"
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.d(TAG, "sendAlert: Network slow, alert saved locally and will sync.")
                _status.value = "Alert queued. Sending in background..."
            } catch (e: Exception) {
                Log.e(TAG, "sendAlert: FAILED - ${e.message}")
                _status.value = "Failed to send alert: ${e.message}"
            } finally {
                _loading.value = false
                Log.d(TAG, "sendAlert: Loading hidden")
            }
        }
    }

    // Health - Optimized
    fun fetchHealthReports() {
        val uid = FirebaseUtils.currentUserUid ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.getHealthReports(uid).get().await()
                    }
                }
                val list = mutableListOf<HealthReport>()
                for (shot in snapshot.children) {
                    shot.getValue(HealthReport::class.java)?.let { list.add(0, it) }
                }
                _healthReports.value = list
            } catch (e: Exception) {
                _status.value = "Failed to load health reports"
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveHealthReport(report: HealthReport) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.saveHealthReport(report).await()
                    }
                }
                _status.value = "Health report saved"
                fetchHealthReports()
            } catch (e: Exception) {
                _status.value = "Failed to save report"
            } finally {
                _loading.value = false
            }
        }
    }

    // Honey - Optimized
    fun fetchHoneyRecords() {
        val uid = FirebaseUtils.currentUserUid ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.getHoneyRecords(uid).get().await()
                    }
                }
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
            } catch (e: Exception) {
                _status.value = "Failed to load honey records"
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveHoneyRecord(record: HoneyRecord) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.saveHoneyRecord(record).await()
                    }
                }
                _status.value = "Honey record saved"
                fetchHoneyRecords()
            } catch (e: Exception) {
                _status.value = "Failed to save record"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteHive(hiveId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.deleteHive(hiveId).await()
                    }
                }
                _status.value = "Hive deleted"
                fetchAllHives()
            } catch (e: Exception) {
                _status.value = "Failed to delete hive"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
