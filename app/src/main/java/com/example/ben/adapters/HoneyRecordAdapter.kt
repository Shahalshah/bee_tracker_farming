package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemHoneyRecordBinding
import com.example.ben.models.HoneyRecord

class HoneyRecordAdapter(private val records: List<HoneyRecord>) : RecyclerView.Adapter<HoneyRecordAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHoneyRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHoneyRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.binding.tvQuantity.text = "${record.quantity} kg"
        holder.binding.tvDate.text = record.date
    }

    override fun getItemCount() = records.size
}
