package com.example.ben.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HoneyRecordAdapter
import com.example.ben.databinding.ActivityHoneyProductionBinding
import com.example.ben.models.HoneyRecord
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class HoneyProductionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHoneyProductionBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HoneyRecordAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoneyProductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        viewModel.fetchHoneyRecords()
    }

    private fun setupRecyclerView() {
        adapter = HoneyRecordAdapter(emptyList()) { recordId ->
            // delete record
        }
        binding.rvProductionHistory.layoutManager = LinearLayoutManager(this)
        binding.rvProductionHistory.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.honeyRecords.observe(this) { list ->
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
        binding.etHarvestDate.setOnClickListener { showDatePicker() }
        binding.btnSaveProduction.setOnClickListener { validateAndSave() }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            binding.etHarvestDate.setText(String.format(Locale.getDefault(), "%02d %s %04d", day, SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(year, month, day) }.time), year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateAndSave() {
        val qty = binding.etHoneyYield.text.toString().toDoubleOrNull() ?: 0.0
        val quality = binding.etQuality.text.toString().trim()
        val date = binding.etHarvestDate.text.toString()
        val notes = binding.etNotes.text.toString().trim()

        if (qty <= 0.0 || date.isEmpty()) {
            Toast.makeText(this, "Quantity and Date are required", Toast.LENGTH_SHORT).show()
            return
        }

        val record = HoneyRecord(
            beekeeperId = FirebaseUtils.currentUserUid ?: "",
            harvestDate = date,
            quantity = qty,
            quality = quality,
            notes = notes
        )

        viewModel.saveHoneyRecord(record)
        
        binding.etHoneyYield.setText("")
        binding.etQuality.setText("")
        binding.etHarvestDate.setText("")
        binding.etNotes.setText("")
    }
}
