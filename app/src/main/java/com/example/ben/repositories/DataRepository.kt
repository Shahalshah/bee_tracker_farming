package com.example.ben.repositories

import com.example.ben.models.*
import com.example.ben.utils.FirebaseUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.database.Query

class DataRepository {
    
    // Hives
    fun saveHive(hive: Hive): Task<Void> {
        return FirebaseUtils.hivesRef().child(hive.id).setValue(hive)
    }

    fun getAllHives(): Query {
        return FirebaseUtils.hivesRef()
    }

    // Alerts
    fun sendAlert(alert: Alert): Task<Void> {
        return FirebaseUtils.alertsRef().child(alert.id).setValue(alert)
    }

    fun getAlerts(): Query {
        return FirebaseUtils.alertsRef().orderByChild("timestamp")
    }

    // Health Reports
    fun saveHealthReport(report: HealthReport): Task<Void> {
        return FirebaseUtils.healthReportsRef().child(report.beekeeperId).child(report.id).setValue(report)
    }

    fun deleteHealthReport(uid: String, reportId: String): Task<Void> {
        return FirebaseUtils.healthReportsRef().child(uid).child(reportId).removeValue()
    }

    fun getHealthReports(uid: String): Query {
        return FirebaseUtils.healthReportsRef().child(uid)
    }

    // Honey Records
    fun saveHoneyRecord(record: HoneyRecord): Task<Void> {
        val ref = FirebaseUtils.database.getReference("honey_production")
        return ref.child(record.beekeeperId).child(record.id).setValue(record)
    }

    fun deleteHoneyRecord(uid: String, recordId: String): Task<Void> {
        val ref = FirebaseUtils.database.getReference("honey_production")
        return ref.child(uid).child(recordId).removeValue()
    }

    fun getHoneyRecords(uid: String): Query {
        return FirebaseUtils.database.getReference("honey_production").child(uid)
    }
}
