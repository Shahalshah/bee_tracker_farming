package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemHoneyRecordBinding
import com.example.ben.models.HoneyRecord

class HoneyRecordAdapter(
    private var records: List<HoneyRecord>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<HoneyRecordAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHoneyRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHoneyRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.binding.tvQuantity.text = "${record.quantity} kg (${record.quality})"
        holder.binding.tvDate.text = record.harvestDate
        
        holder.binding.btnDelete.setOnClickListener {
            onDelete(record.id)
        }
    }

    override fun getItemCount() = records.size

    fun updateData(newList: List<HoneyRecord>) {
        records = newList
        notifyDataSetChanged()
    }
}
