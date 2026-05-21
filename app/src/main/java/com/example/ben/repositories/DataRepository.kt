package com.example.ben.repositories

import android.util.Log
import com.example.ben.models.*
import com.example.ben.utils.FirebaseUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.database.Query

class DataRepository {
    private val TAG = "DataRepositoryDebug"
    
    // Hives
    fun saveHive(hive: Hive): Task<Void> {
        val ref = FirebaseUtils.hivesRef()
        val hiveId = if (hive.id.isEmpty()) ref.push().key ?: "" else hive.id
        
        if (hiveId.isEmpty()) {
            Log.e(TAG, "saveHive: Failed to generate hive ID")
            throw IllegalStateException("Failed to generate Hive ID.")
        }
        
        val finalHive = hive.copy(id = hiveId, timestamp = System.currentTimeMillis())
        Log.d(TAG, "saveHive: Saving to path: hives/$hiveId data: $finalHive")
        
        return ref.child(hiveId).setValue(finalHive)
    }

    fun deleteHive(hiveId: String): Task<Void> {
        return FirebaseUtils.hivesRef().child(hiveId).removeValue()
    }

    fun getAllHives(): Query {
        return FirebaseUtils.hivesRef()
    }

    fun getBeekeeperHives(beekeeperId: String): Query {
        return FirebaseUtils.hivesRef().orderByChild("beekeeperId").equalTo(beekeeperId)
    }

    // Alerts
    fun sendAlert(alert: Alert): Task<Void> {
        val ref = FirebaseUtils.alertsRef()
        val id = ref.push().key ?: ""
        if (id.isEmpty()) throw IllegalStateException("Failed to generate Alert ID.")
        
        val finalAlert = alert.copy(id = id, timestamp = System.currentTimeMillis())
        Log.d(TAG, "sendAlert: Saving to alerts/$id")
        
        return ref.child(id).setValue(finalAlert)
    }

    fun getAlerts(): Query {
        return FirebaseUtils.alertsRef().orderByChild("timestamp")
    }

    // Health Reports
    fun saveHealthReport(report: HealthReport): Task<Void> {
        val ref = FirebaseUtils.healthReportsRef().child(report.beekeeperId)
        val id = if (report.id.isEmpty()) ref.push().key ?: "" else report.id
        if (id.isEmpty()) throw IllegalStateException("Failed to generate Report ID.")
        
        val finalReport = report.copy(id = id, timestamp = System.currentTimeMillis())
        return ref.child(id).setValue(finalReport)
    }

    fun deleteHealthReport(beekeeperId: String, reportId: String): Task<Void> {
        return FirebaseUtils.healthReportsRef().child(beekeeperId).child(reportId).removeValue()
    }

    fun getHealthReports(beekeeperId: String): Query {
        return FirebaseUtils.healthReportsRef().child(beekeeperId).orderByChild("timestamp")
    }

    // Honey Records
    fun saveHoneyRecord(record: HoneyRecord): Task<Void> {
        val ref = FirebaseUtils.honeyProductionRef().child(record.beekeeperId)
        val id = if (record.id.isEmpty()) ref.push().key ?: "" else record.id
        if (id.isEmpty()) throw IllegalStateException("Failed to generate Record ID.")
        
        val finalRecord = record.copy(id = id, timestamp = System.currentTimeMillis())
        return ref.child(id).setValue(finalRecord)
    }

    fun deleteHoneyRecord(beekeeperId: String, recordId: String): Task<Void> {
        return FirebaseUtils.honeyProductionRef().child(beekeeperId).child(recordId).removeValue()
    }

    fun getHoneyRecords(beekeeperId: String): Query {
        return FirebaseUtils.honeyProductionRef().child(beekeeperId).orderByChild("timestamp")
    }
}
