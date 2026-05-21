package com.example.ben.repositories

import com.example.ben.models.*
import com.example.ben.utils.FirebaseUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.database.Query

class DataRepository {
    
    // Hives
    fun saveHive(hive: Hive): Task<Void> {
        val ref = FirebaseUtils.hivesRef()
        // Check if we already have an ID, otherwise create one
        val hiveId = if (hive.id.isEmpty()) ref.push().key ?: "" else hive.id
        if (hiveId.isEmpty()) throw IllegalStateException("Failed to generate Hive ID. Check Database connection.")
        
        return ref.child(hiveId).setValue(hive.copy(id = hiveId))
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
        return ref.child(id).setValue(alert.copy(id = id, timestamp = System.currentTimeMillis()))
    }

    fun getAlerts(): Query {
        return FirebaseUtils.alertsRef().orderByChild("timestamp")
    }

    // Health Reports
    fun saveHealthReport(report: HealthReport): Task<Void> {
        val ref = FirebaseUtils.healthReportsRef().child(report.beekeeperId)
        val id = if (report.id.isEmpty()) ref.push().key ?: "" else report.id
        return ref.child(id).setValue(report.copy(id = id, timestamp = System.currentTimeMillis()))
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
        return ref.child(id).setValue(record.copy(id = id, timestamp = System.currentTimeMillis()))
    }

    fun deleteHoneyRecord(beekeeperId: String, recordId: String): Task<Void> {
        return FirebaseUtils.honeyProductionRef().child(beekeeperId).child(recordId).removeValue()
    }

    fun getHoneyRecords(beekeeperId: String): Query {
        return FirebaseUtils.honeyProductionRef().child(beekeeperId).orderByChild("timestamp")
    }
}
