package com.example.ben.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.AlertAdapter
import com.example.ben.databinding.FragmentAlertsBinding
import com.example.ben.viewmodels.AlertViewModel

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlertViewModel by viewModels()
    private lateinit var adapter: AlertAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.fetchAlerts()
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(emptyList())
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.alerts.observe(viewLifecycleOwner) { list ->
            adapter = AlertAdapter(list)
            binding.rvAlerts.adapter = adapter
            binding.tvNoAlerts.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
