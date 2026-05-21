package com.example.ben.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HoneyRecordAdapter
import com.example.ben.databinding.FragmentHoneyBinding
import com.example.ben.models.HoneyRecord
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class HoneyFragment : Fragment() {

    private var _binding: FragmentHoneyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: HoneyRecordAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.fetchHoneyRecords()
    }

    private fun setupRecyclerView() {
        adapter = HoneyRecordAdapter(emptyList()) { recordId ->
            // Delete record
        }
        binding.rvProductionHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductionHistory.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.honeyRecords.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.etHarvestDate.setOnClickListener { showDatePicker() }
        binding.btnSaveProduction.setOnClickListener { validateAndSave() }
    }

    private fun showDatePicker() {
        DatePickerDialog(requireContext(), { _, year, month, day ->
            binding.etHarvestDate.setText(String.format(Locale.getDefault(), "%02d %s %04d", day, SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(year, month, day) }.time), year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateAndSave() {
        val qty = binding.etHoneyYield.text.toString().toDoubleOrNull() ?: 0.0
        val quality = binding.etQuality.text.toString().trim()
        val date = binding.etHarvestDate.text.toString()
        val notes = binding.etNotes.text.toString().trim()

        if (qty <= 0.0 || date.isEmpty()) {
            Toast.makeText(requireContext(), "Quantity and Date are required", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
