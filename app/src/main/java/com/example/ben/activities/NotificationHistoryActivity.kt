package com.example.ben.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ben.adapters.AlertAdapter
import com.example.ben.databinding.ActivityNotificationHistoryBinding
import com.example.ben.models.Alert
import com.example.ben.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NotificationHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationHistoryBinding
    private val alertList = mutableListOf<Alert>()
    private lateinit var adapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(alertList)
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        FirebaseUtils.alertsRef().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alertList.clear()
                for (alertSnapshot in snapshot.children) {
                    val alert = alertSnapshot.getValue(Alert::class.java)
                    alert?.let { alertList.add(0, it) } // Newest first
                }
                adapter.notifyDataSetChanged()
                
                if (alertList.isEmpty()) {
                    Toast.makeText(this@NotificationHistoryActivity, "No notifications found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NotificationHistoryActivity, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
