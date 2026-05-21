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
            }
        }

        mainViewModel.status.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                mainViewModel.clearStatus()
            }
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
        // Card 1: Hive Management (Add Hive Location)
        binding.tvAction1.text = getString(R.string.add_hive_location)
        binding.tvAction1Sub.text = "Manage your hives"
        binding.ivAction1.setImageResource(android.R.drawable.ic_menu_add)
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        // Card 2: Hive Health Tracker
        binding.tvAction2.text = getString(R.string.health_tracker)
        binding.tvAction2Sub.text = "Monitor condition"
        binding.ivAction2.setImageResource(android.R.drawable.ic_menu_edit)
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        // Card 3: Honey Production Records
        binding.tvAction3.text = getString(R.string.honey_production)
        binding.tvAction3Sub.text = "Track production"
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_save)
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_honey))

        // Card 4: Notification History (Spray Alerts)
        binding.tvAction4.text = "Notification History"
        binding.tvAction4Sub.text = "View alerts"
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.cardAction4.setCardBackgroundColor(requireContext().getColor(R.color.card_action_history))
    }

    private fun setupFarmerDashboard() {
        // Card 1: Spray Alert
        binding.tvAction1.text = getString(R.string.spray_alert)
        binding.tvAction1Sub.text = getString(R.string.spraying_today_sub)
        binding.ivAction1.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_alert))

        // Card 2: View Hive Map (Nearby Hive Detection)
        binding.tvAction2.text = getString(R.string.view_hive_map)
        binding.tvAction2Sub.text = getString(R.string.nearby_hives_sub)
        binding.ivAction2.setImageResource(android.R.drawable.ic_dialog_map)
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        // Card 3: Safety Tips (Bee-Friendly pesticide suggestions)
        binding.tvAction3.text = getString(R.string.bee_tips)
        binding.tvAction3Sub.text = "Bee-friendly tips"
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_info_details)
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        // Card 4: Notification History (Spray History)
        binding.tvAction4.text = "Notification History"
        binding.tvAction4Sub.text = "View alerts"
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.cardAction4.setCardBackgroundColor(requireContext().getColor(R.color.card_action_history))
    }

    private fun setupClickListeners() {
        binding.cardAction1.setOnClickListener {
            if (userRole == "Farmer") startActivity(Intent(requireContext(), AlertActivity::class.java))
            else {
                val intent = Intent(requireContext(), MapActivity::class.java)
                intent.putExtra("ACTION", "ADD_HIVE")
                startActivity(intent)
            }
        }
        binding.cardAction2.setOnClickListener {
            startActivity(Intent(requireContext(), MapActivity::class.java))
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
