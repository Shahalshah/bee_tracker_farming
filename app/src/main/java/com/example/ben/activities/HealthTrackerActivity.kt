package com.example.ben.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HealthReportAdapter
import com.example.ben.databinding.ActivityHealthTrackerBinding
import com.example.ben.models.HealthReport
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class HealthTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHealthTrackerBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HealthReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        viewModel.fetchHealthReports()
    }

    private fun setupRecyclerView() {
        adapter = HealthReportAdapter(emptyList()) { reportId ->
            FirebaseUtils.currentUserUid?.let { uid ->
                // delete report
            }
        }
        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.healthReports.observe(this) { list ->
            adapter.updateData(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.status.observe(this) { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnAddReport.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val hiveId = binding.etHiveId.text.toString().trim()
        val condition = binding.etCondition.text.toString().trim()
        val diseases = binding.etDiseases.text.toString().trim()
        val population = binding.etPopulation.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (hiveId.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Hive ID and Condition are required", Toast.LENGTH_SHORT).show()
            return
        }

        val report = HealthReport(
            beekeeperId = FirebaseUtils.currentUserUid ?: "",
            hiveId = hiveId,
            colonyCondition = condition,
            diseases = diseases,
            population = population,
            notes = notes,
            date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        )

        viewModel.saveHealthReport(report)
        
        // Clear fields
        binding.etHiveId.setText("")
        binding.etCondition.setText("")
        binding.etDiseases.setText("")
        binding.etPopulation.setText("")
        binding.etNotes.setText("")
    }
}
