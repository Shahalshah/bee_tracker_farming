package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemAlertBinding
import com.example.ben.models.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter(private val alerts: List<Alert>) : RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = alerts[position]
        holder.binding.tvAlertTitle.text = "Spray Alert: ${alert.pesticideName}"
        holder.binding.tvAlertMessage.text = "Farmer ${alert.farmerName} is spraying on ${alert.sprayDate} at ${alert.sprayTime}"
        
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
        holder.binding.tvAlertTime.text = "Sent: $date"
        
        if (alert.notes.isNotEmpty()) {
            holder.binding.tvAlertMessage.text = "${holder.binding.tvAlertMessage.text}\nNotes: ${alert.notes}"
        }
    }

    override fun getItemCount() = alerts.size
}
