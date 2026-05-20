package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemHealthReportBinding
import com.example.ben.models.HealthReport

class HealthReportAdapter(
    private var reports: List<HealthReport>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<HealthReportAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHealthReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.tvDate.text = report.date
        holder.binding.tvStats.text = "Hive: ${report.hiveId} | ${report.colonyCondition}"
        holder.binding.tvNotes.text = "${report.diseases}\n${report.notes}"
        
        holder.binding.btnDelete.setOnClickListener {
            onDelete(report.id)
        }
    }

    override fun getItemCount() = reports.size

    fun updateData(newList: List<HealthReport>) {
        reports = newList
        notifyDataSetChanged()
    }
}
