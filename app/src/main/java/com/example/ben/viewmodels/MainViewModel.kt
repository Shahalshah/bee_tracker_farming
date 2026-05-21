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

    private var hivesListener: ValueEventListener? = null
    private var alertsListener: ValueEventListener? = null

    // Hives - Fast Loading & Marker Sync
    fun fetchAllHives() {
        val uid = FirebaseUtils.currentUserUid ?: return
        Log.d(TAG, "fetchAllHives: Requesting hives from Firebase...")
        
        if (hivesListener != null) {
            repository.getAllHives().removeEventListener(hivesListener!!)
        }

        hivesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "fetchAllHives: Firebase Data Received. Children count: ${snapshot.childrenCount}")
                val list = mutableListOf<Hive>()
                for (shot in snapshot.children) {
                    try {
                        val hive = shot.getValue(Hive::class.java)
                        if (hive != null) {
                            list.add(hive)
                        } else {
                            Log.e(TAG, "fetchAllHives: Failed to map hive data for shot: ${shot.key}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "fetchAllHives: Error parsing hive: ${e.message}")
                    }
                }
                
                _hives.postValue(list)
                _hiveCount.postValue(list.count { it.beekeeperId == uid })
                _nearbyHivesCount.postValue(list.size)
                
                Log.d(TAG, "fetchAllHives: Successfully updated LiveData with ${list.size} hives")
                _loading.postValue(false)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "fetchAllHives: Firebase Error - ${error.message}")
                _status.postValue("Failed to load map data: ${error.message}")
                _loading.postValue(false)
            }
        }
        
        _loading.value = true
        repository.getAllHives().addValueEventListener(hivesListener!!)
    }

    fun saveHive(hive: Hive) {
        val uid = FirebaseUtils.currentUserUid
        if (uid == null) {
            _status.value = "Session expired. Please log in."
            return
        }

        Log.d(TAG, "saveHive: Started for ${hive.name}")
        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.saveHive(hive).await()
                    }
                }
                Log.d(TAG, "saveHive: Success")
                _status.value = "Hive location saved successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "saveHive: Failed - ${e.message}")
                _status.value = "Failed to save hive: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Alerts - Real-time sync
    fun fetchAlerts() {
        val uid = FirebaseUtils.currentUserUid ?: return
        if (alertsListener != null) {
            repository.getAlerts().removeEventListener(alertsListener!!)
        }

        alertsListener = object : ValueEventListener {
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
                _alerts.postValue(list)
                _activeAlertsCount.postValue(activeCount)
                _alertsSentCount.postValue(sentByMe)
            }

            override fun onCancelled(error: DatabaseError) {
                _status.postValue("Failed to sync alerts")
            }
        }
        repository.getAlerts().addValueEventListener(alertsListener!!)
    }

    fun sendAlert(alert: Alert) {
        viewModelScope.launch {
            _loading.value = true
            try {
                withTimeout(5000L) {
                    withContext(Dispatchers.IO) {
                        repository.sendAlert(alert).await()
                    }
                }
                _status.value = "Alert sent successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "sendAlert: Failed - ${e.message}")
                _status.value = "Failed to send alert"
            } finally {
                _loading.value = false
            }
        }
    }

    // Common Cleanup
    fun clearStatus() {
        _status.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup listeners to prevent memory leaks
        hivesListener?.let { repository.getAllHives().removeEventListener(it) }
        alertsListener?.let { repository.getAlerts().removeEventListener(it) }
    }
    
    // Additional features (Honey/Health) - Single Fetch for Performance
    fun fetchHealthReports() {
        val uid = FirebaseUtils.currentUserUid ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = repository.getHealthReports(uid).get().await()
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
                repository.saveHealthReport(report).await()
                _status.value = "Health report saved"
                fetchHealthReports()
            } catch (e: Exception) {
                _status.value = "Failed to save report"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchHoneyRecords() {
        val uid = FirebaseUtils.currentUserUid ?: return
        viewModelScope.launch {
            _loading.value = true
            try {
                val snapshot = repository.getHoneyRecords(uid).get().await()
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
                repository.saveHoneyRecord(record).await()
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
                _status.value = "Hive deleted successfully"
            } catch (e: Exception) {
                _status.value = "Failed to delete hive"
            } finally {
                _loading.value = false
            }
        }
    }
}
