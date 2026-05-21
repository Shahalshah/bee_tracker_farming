package com.example.ben.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HealthReportAdapter
import com.example.ben.databinding.FragmentHealthBinding
import com.example.ben.models.HealthReport
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: HealthReportAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        viewModel.fetchHealthReports()
    }

    private fun setupRecyclerView() {
        adapter = HealthReportAdapter(emptyList()) { reportId ->
            // Delete report
            FirebaseUtils.currentUserUid?.let { uid ->
                // Implementation for delete if needed
            }
        }
        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReports.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.healthReports.observe(viewLifecycleOwner) { list ->
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
        binding.btnAddReport.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() {
        val hiveId = binding.etHiveId.text.toString().trim()
        val condition = binding.etCondition.text.toString().trim()
        val diseases = binding.etDiseases.text.toString().trim()
        val population = binding.etPopulation.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (hiveId.isEmpty() || condition.isEmpty()) {
            Toast.makeText(requireContext(), "Hive ID and Condition are required", Toast.LENGTH_SHORT).show()
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
        
        binding.etHiveId.setText("")
        binding.etCondition.setText("")
        binding.etDiseases.setText("")
        binding.etPopulation.setText("")
        binding.etNotes.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
