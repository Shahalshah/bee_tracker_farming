package com.example.ben.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ben.R
import com.example.ben.activities.*
import com.example.ben.databinding.FragmentHomeBinding
import com.example.ben.viewmodels.AuthViewModel
import com.example.ben.viewmodels.MainViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    
    private var userRole: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userRole = user.role
                updateUI(user.name)
                
                // Show stats for everyone
                binding.layoutStats.visibility = View.VISIBLE
                updateLabels()
                // Update values immediately in case they were fetched before role
                updateStatValues()
            }
        }

        mainViewModel.hiveCount.observe(viewLifecycleOwner) { updateStatValues() }
        mainViewModel.nearbyHivesCount.observe(viewLifecycleOwner) { updateStatValues() }
        mainViewModel.totalHoney.observe(viewLifecycleOwner) { updateStatValues() }
        mainViewModel.activeAlertsCount.observe(viewLifecycleOwner) { updateStatValues() }
        mainViewModel.alertsSentCount.observe(viewLifecycleOwner) { updateStatValues() }

        mainViewModel.status.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                mainViewModel.clearStatus()
            }
        }
    }

    private fun updateStatValues() {
        if (userRole.isEmpty()) return

        if (userRole == "Beekeeper") {
            binding.tvStat1Value.text = (mainViewModel.hiveCount.value ?: 0).toString()
            binding.tvStat2Value.text = (mainViewModel.activeAlertsCount.value ?: 0).toString()
            binding.tvStat3Value.text = java.util.Locale.getDefault().let { locale ->
                String.format(locale, "%.1f", mainViewModel.totalHoney.value ?: 0.0)
            }
        } else {
            binding.tvStat1Value.text = (mainViewModel.nearbyHivesCount.value ?: 0).toString()
            binding.tvStat2Value.text = (mainViewModel.alertsSentCount.value ?: 0).toString()
            binding.tvStat3Value.text = "12" // Placeholder for Tips
        }
    }

    private fun updateLabels() {
        if (userRole == "Beekeeper") {
            binding.tvStat1Label.text = "Total Hives"
            binding.tvStat2Label.text = "Active Alerts"
            binding.tvStat3Label.text = "Honey (kg)"
        } else {
            binding.tvStat1Label.text = "Nearby Hives"
            binding.tvStat2Label.text = "Alerts Sent"
            binding.tvStat3Label.text = "Tips"
            binding.tvStat3Value.text = "12" // Placeholder for Tips count
        }
    }

    private fun updateUI(name: String) {
        if (userRole == "Beekeeper") {
            binding.tvWelcome.text = getString(R.string.hello_beekeeper)
            binding.tvBannerText.text = getString(R.string.beekeeper_banner)
            setupBeekeeperDashboard()
        } else {
            binding.tvWelcome.text = getString(R.string.hello_farmer)
            binding.tvBannerText.text = getString(R.string.farmer_banner)
            setupFarmerDashboard()
        }
    }

    private fun setupBeekeeperDashboard() {
        binding.tvAction1.text = "Manage Hives"
        binding.tvAction1Sub.text = "Add, Edit, or Delete"
        binding.ivAction1.setImageResource(android.R.drawable.ic_menu_add)
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        binding.tvAction2.text = getString(R.string.health_tracker)
        binding.tvAction2Sub.text = "Monitor condition"
        binding.ivAction2.setImageResource(android.R.drawable.ic_menu_edit)
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        binding.tvAction3.text = getString(R.string.honey_production)
        binding.tvAction3Sub.text = "Track production"
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_save)
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_honey))

        binding.tvAction4.text = "Notification History"
        binding.tvAction4Sub.text = "View alerts"
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.cardAction4.setCardBackgroundColor(requireContext().getColor(R.color.card_action_history))
    }

    private fun setupFarmerDashboard() {
        binding.tvAction1.text = getString(R.string.spray_alert)
        binding.tvAction1Sub.text = getString(R.string.spraying_today_sub)
        binding.ivAction1.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_alert))

        binding.tvAction2.text = getString(R.string.view_hive_map)
        binding.tvAction2Sub.text = getString(R.string.nearby_hives_sub)
        binding.ivAction2.setImageResource(android.R.drawable.ic_dialog_map)
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        binding.tvAction3.text = getString(R.string.bee_tips)
        binding.tvAction3Sub.text = "Bee-friendly tips"
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_info_details)
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        binding.tvAction4.text = "Notification History"
        binding.tvAction4Sub.text = "View alerts"
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.cardAction4.setCardBackgroundColor(requireContext().getColor(R.color.card_action_history))
    }

    private fun setupClickListeners() {
        binding.cardAction1.setOnClickListener {
            if (userRole == "Farmer") startActivity(Intent(requireContext(), AlertActivity::class.java))
            else startActivity(Intent(requireContext(), ManageHivesActivity::class.java))
        }
        binding.cardAction2.setOnClickListener {
            if (userRole == "Beekeeper") startActivity(Intent(requireContext(), HealthTrackerActivity::class.java))
            else startActivity(Intent(requireContext(), MapActivity::class.java))
        }
        binding.cardAction3.setOnClickListener {
            if (userRole == "Farmer") startActivity(Intent(requireContext(), TipsActivity::class.java))
            else startActivity(Intent(requireContext(), HoneyProductionActivity::class.java))
        }
        binding.cardAction4.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationHistoryActivity::class.java))
        }
        
        binding.ivUserAvatar.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
