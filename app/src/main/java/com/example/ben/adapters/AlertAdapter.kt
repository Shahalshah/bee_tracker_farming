package com.example.ben.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemAlertBinding
import com.example.ben.models.Alert
import java.util.*

class AlertAdapter(private val alerts: List<Alert>) : RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = alerts[position]
        holder.binding.tvAlertTitle.text = "Spray Alert"
        holder.binding.tvAlertMessage.text = "Farmer ${alert.farmerName} is spraying today at ${alert.time}"
        
        val dateString = DateFormat.format("dd MMM yyyy • hh:mm a", Date(alert.timestamp)).toString()
        holder.binding.tvAlertTime.text = dateString
    }

    override fun getItemCount() = alerts.size
}
