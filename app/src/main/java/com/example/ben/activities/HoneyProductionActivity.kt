package com.example.ben.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HoneyRecordAdapter
import com.example.ben.databinding.ActivityHoneyProductionBinding
import com.example.ben.models.HoneyRecord
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HoneyProductionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHoneyProductionBinding
    private val productionList = mutableListOf<HoneyRecord>()
    private lateinit var adapter: HoneyRecordAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHoneyProductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadProductionRecords()

        binding.etHarvestDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSaveProduction.setOnClickListener {
            saveRecord()
        }
    }

    private fun setupRecyclerView() {
        adapter = HoneyRecordAdapter(productionList)
        binding.rvProductionHistory.layoutManager = LinearLayoutManager(this)
        binding.rvProductionHistory.adapter = adapter
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.etHarvestDate.setText(format.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun loadProductionRecords() {
        val uid = FirebaseUtils.currentUserUid ?: return
        FirebaseUtils.database.getReference("honey_production").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productionList.clear()
                    for (recordSnapshot in snapshot.children) {
                        val record = recordSnapshot.getValue(HoneyRecord::class.java)
                        record?.let { productionList.add(0, it) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HoneyProductionActivity, "Failed to load records", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveRecord() {
        val quantityStr = binding.etHoneyYield.text.toString()
        val date = binding.etHarvestDate.text.toString()

        if (quantityStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toDoubleOrNull() ?: 0.0
        val uid = FirebaseUtils.currentUserUid ?: return
        val ref = FirebaseUtils.database.getReference("honey_production").child(uid)
        val recordId = ref.push().key ?: return
        
        val record = HoneyRecord(recordId, uid, quantity, date)

        ref.child(recordId).setValue(record)
            .addOnSuccessListener {
                binding.etHoneyYield.setText("")
                binding.etHarvestDate.setText("")
                Toast.makeText(this, "Record saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save record", Toast.LENGTH_SHORT).show()
            }
    }
}
