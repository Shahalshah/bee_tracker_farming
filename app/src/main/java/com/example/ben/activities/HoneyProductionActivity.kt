package com.example.ben.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HoneyRecordAdapter
import com.example.ben.databinding.ActivityHoneyProductionBinding
import com.example.ben.models.HoneyRecord
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.HoneyViewModel
import java.text.SimpleDateFormat
import java.util.*

class HoneyProductionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHoneyProductionBinding
    private val viewModel: HoneyViewModel by viewModels()
    private lateinit var adapter: HoneyRecordAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoneyProductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchRecords()

        binding.etHarvestDate.setOnClickListener { showDatePicker() }
        binding.btnSaveProduction.setOnClickListener { saveRecord() }
    }

    private fun setupRecyclerView() {
        adapter = HoneyRecordAdapter(emptyList()) { recordId ->
            viewModel.deleteRecord(recordId)
        }
        binding.rvProductionHistory.layoutManager = LinearLayoutManager(this)
        binding.rvProductionHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.records.observe(this) { list ->
            adapter.updateData(list)
        }
        viewModel.status.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, day ->
            val selected = Calendar.getInstance()
            selected.set(year, month, day)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.etHarvestDate.setText(format.format(selected.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveRecord() {
        val qty = binding.etHoneyYield.text.toString().toDoubleOrNull() ?: 0.0
        val quality = binding.etQuality.text.toString().trim()
        val date = binding.etHarvestDate.text.toString()
        val notes = binding.etNotes.text.toString().trim()

        if (qty == 0.0 || date.isEmpty()) {
            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseUtils.currentUserUid ?: return
        val recordId = FirebaseUtils.database.getReference("honey_production").child(uid).push().key ?: return
        
        val record = HoneyRecord(recordId, uid, date, qty, quality, notes)
        viewModel.saveRecord(record)
        
        binding.etHoneyYield.setText("")
        binding.etQuality.setText("")
        binding.etHarvestDate.setText("")
        binding.etNotes.setText("")
    }
}
