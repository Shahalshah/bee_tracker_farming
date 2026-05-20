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
        holder.binding.tvAlertTitle.text = "Spray Alert: ${alert.farmerName}"
        holder.binding.tvAlertMessage.text = alert.message
        
        val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
        holder.binding.tvAlertTime.text = date
        
        if (alert.pesticide.isNotEmpty()) {
            holder.binding.tvAlertMessage.text = "${alert.message}\nPesticide: ${alert.pesticide}"
        }
    }

    override fun getItemCount() = alerts.size
}
