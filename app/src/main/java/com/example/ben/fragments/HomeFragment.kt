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
import com.example.ben.models.User
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.AuthViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()
    private var userRole: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                userRole = user.role
                updateUI(user)
            }
        }
        
        authViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (isAdded && msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: User) {
        if (userRole == "Beekeeper") {
            binding.tvWelcome.text = getString(R.string.hello_beekeeper)
            binding.tvBannerText.text = getString(R.string.beekeeper_banner)
            setupBeekeeperUI()
        } else {
            binding.tvWelcome.text = getString(R.string.hello_farmer)
            binding.tvBannerText.text = getString(R.string.farmer_banner)
            setupFarmerUI()
        }
    }

    private fun setupBeekeeperUI() {
        // Card 1: Add Hive Location
        binding.tvAction1.text = getString(R.string.add_hive_location)
        binding.tvAction1Sub.text = ""
        binding.ivAction1.setImageResource(android.R.drawable.ic_menu_add)
        binding.ivAction1.setColorFilter(requireContext().getColor(R.color.primary_green))
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        // Card 2: Health Tracker
        binding.tvAction2.text = getString(R.string.health_tracker)
        binding.tvAction2Sub.text = ""
        binding.ivAction2.setImageResource(android.R.drawable.ic_menu_edit)
        binding.ivAction2.setColorFilter(requireContext().getColor(R.color.honey_orange))
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        // Card 3: Honey Production
        binding.tvAction3.text = getString(R.string.honey_production)
        binding.tvAction3Sub.text = ""
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_save)
        binding.ivAction3.setColorFilter(requireContext().getColor(R.color.primary_dark))
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_honey))

        // Card 4: Notification History
        binding.tvAction4.text = getString(R.string.notification_history)
        binding.tvAction4Sub.text = ""
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.ivAction4.setColorFilter(requireContext().getColor(R.color.honey_orange))
        binding.cardAction4.setCardBackgroundColor(requireContext().getColor(R.color.card_action_history))
    }

    private fun setupFarmerUI() {
        // Card 1: Spray Alert
        binding.tvAction1.text = getString(R.string.spray_alert)
        binding.tvAction1Sub.text = getString(R.string.spraying_today_sub)
        binding.ivAction1.setImageResource(android.R.drawable.ic_dialog_alert)
        binding.ivAction1.setColorFilter(requireContext().getColor(R.color.alert_red))
        binding.cardAction1.setCardBackgroundColor(requireContext().getColor(R.color.card_action_alert))

        // Card 2: View Hive Map
        binding.tvAction2.text = getString(R.string.view_hive_map)
        binding.tvAction2Sub.text = getString(R.string.nearby_hives_sub)
        binding.ivAction2.setImageResource(android.R.drawable.ic_dialog_map)
        binding.ivAction2.setColorFilter(requireContext().getColor(R.color.primary_green))
        binding.cardAction2.setCardBackgroundColor(requireContext().getColor(R.color.card_action_map))

        // Card 3: Bee-Friendly Tips
        binding.tvAction3.text = getString(R.string.bee_tips)
        binding.tvAction3Sub.text = getString(R.string.learn_protect)
        binding.ivAction3.setImageResource(android.R.drawable.ic_menu_info_details)
        binding.ivAction3.setColorFilter(requireContext().getColor(R.color.honey_orange))
        binding.cardAction3.setCardBackgroundColor(requireContext().getColor(R.color.card_action_tips))

        // Card 4: Notification History
        binding.tvAction4.text = getString(R.string.notification_history)
        binding.tvAction4Sub.text = ""
        binding.ivAction4.setImageResource(android.R.drawable.ic_popup_reminder)
        binding.ivAction4.setColorFilter(requireContext().getColor(R.color.honey_orange))
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
            if (userRole == "Farmer") startActivity(Intent(requireContext(), MapActivity::class.java))
            else startActivity(Intent(requireContext(), HealthTrackerActivity::class.java))
        }
        binding.cardAction3.setOnClickListener {
            if (userRole == "Farmer") startActivity(Intent(requireContext(), TipsActivity::class.java))
            else startActivity(Intent(requireContext(), HoneyProductionActivity::class.java))
        }
        binding.cardAction4.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationHistoryActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
