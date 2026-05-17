package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemHealthReportBinding
import com.example.ben.models.HealthReport

class HealthReportAdapter(private val reports: List<HealthReport>) : RecyclerView.Adapter<HealthReportAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHealthReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.tvDate.text = report.date
        holder.binding.tvStats.text = "Honey: ${report.honeyProduced}kg | Status: ${report.healthStatus}"
        holder.binding.tvNotes.text = report.notes
    }

    override fun getItemCount() = reports.size
}
