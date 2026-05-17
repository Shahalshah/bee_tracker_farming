package com.example.ben.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HealthReportAdapter
import com.example.ben.databinding.ActivityHealthTrackerBinding
import com.example.ben.models.HealthReport
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthTrackerBinding
    private val reportList = mutableListOf<HealthReport>()
    private lateinit var adapter: HealthReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadReports()

        binding.btnAddReport.setOnClickListener {
            saveReport()
        }
    }

    private fun setupRecyclerView() {
        adapter = HealthReportAdapter(reportList)
        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = adapter
    }

    private fun loadReports() {
        val uid = FirebaseUtils.currentUserUid ?: return
        FirebaseUtils.healthReportsRef().child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportList.clear()
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(HealthReport::class.java)
                    report?.let { reportList.add(0, it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HealthTrackerActivity, "Failed to load reports", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveReport() {
        val honey = binding.etHoney.text.toString().toDoubleOrNull() ?: 0.0
        val status = binding.etStatus.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (status.isEmpty()) {
            Toast.makeText(this, "Please enter status", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseUtils.currentUserUid ?: return
        val reportId = FirebaseUtils.healthReportsRef().child(uid).push().key ?: return
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        
        val report = HealthReport(reportId, uid, date, honey, status, notes)

        FirebaseUtils.healthReportsRef().child(uid).child(reportId).setValue(report)
            .addOnSuccessListener {
                binding.etHoney.setText("")
                binding.etStatus.setText("")
                binding.etNotes.setText("")
                Toast.makeText(this, "Report saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save report: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
