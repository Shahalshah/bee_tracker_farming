package com.example.ben.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.HiveAdapter
import com.example.ben.databinding.ActivityManageHivesBinding
import com.example.ben.utils.FirebaseUtils
import com.example.ben.viewmodels.MainViewModel

class ManageHivesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageHivesBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: HiveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageHivesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        
        viewModel.fetchAllHives()
        
        binding.fabAddHive.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("ACTION", "ADD_HIVE")
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = HiveAdapter(emptyList(), 
            onEdit = { hive ->
                // Edit hive logic
            },
            onDelete = { hiveId ->
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Hive")
                    .setMessage("Are you sure you want to delete this hive?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteHive(hiveId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onViewOnMap = { hive ->
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("LAT", hive.latitude)
                intent.putExtra("LNG", hive.longitude)
                startActivity(intent)
            }
        )
        binding.rvHives.layoutManager = LinearLayoutManager(this)
        binding.rvHives.adapter = adapter
    }

    private fun setupObservers() {
        val uid = FirebaseUtils.currentUserUid
        viewModel.hives.observe(this) { hives ->
            val beekeeperHives = hives.filter { it.beekeeperId == uid }
            adapter.updateData(beekeeperHives)
            binding.tvEmptyState.visibility = if (beekeeperHives.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Ensure stats are updated
        viewModel.fetchHoneyRecords()
        viewModel.fetchAlerts()

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
}
