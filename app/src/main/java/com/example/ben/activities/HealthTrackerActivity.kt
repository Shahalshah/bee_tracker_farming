package com.example.ben.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HealthReportAdapter
import com.example.ben.databinding.ActivityHealthTrackerBinding
import com.example.ben.models.HealthReport
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.HealthViewModel
import java.text.SimpleDateFormat
import java.util.*

class HealthTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthTrackerBinding
    private val viewModel: HealthViewModel by viewModels()
    private lateinit var adapter: HealthReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchReports()

        binding.btnAddReport.setOnClickListener {
            saveReport()
        }
    }

    private fun setupRecyclerView() {
        adapter = HealthReportAdapter(emptyList()) { reportId ->
            viewModel.deleteReport(reportId)
        }
        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.reports.observe(this) { list ->
            adapter.updateData(list)
        }
        viewModel.status.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveReport() {
        val hiveId = binding.etHiveId.text.toString().trim()
        val condition = binding.etCondition.text.toString().trim()
        val diseases = binding.etDiseases.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (hiveId.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseUtils.currentUserUid ?: return
        val reportId = FirebaseUtils.healthReportsRef().child(uid).push().key ?: return
        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        
        val report = HealthReport(reportId, uid, hiveId, date, condition, diseases, notes)
        viewModel.saveReport(report)
        
        // Clear fields
        binding.etHiveId.setText("")
        binding.etCondition.setText("")
        binding.etDiseases.setText("")
        binding.etNotes.setText("")
    }
}
