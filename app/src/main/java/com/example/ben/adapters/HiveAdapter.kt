package com.example.ben.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ben.databinding.ItemHiveBinding
import com.example.ben.models.Hive

class HiveAdapter(
    private var hives: List<Hive>,
    private val onEdit: (Hive) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onViewOnMap: (Hive) -> Unit
) : RecyclerView.Adapter<HiveAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHiveBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hive = hives[position]
        holder.binding.tvHiveName.text = hive.name
        holder.binding.tvHiveStatus.text = "Status: ${hive.status}"
        holder.binding.tvHiveDetails.text = "Pop: ${hive.population}\nCond: ${hive.colonyCondition}"

        holder.binding.btnDelete.setOnClickListener { onDelete(hive.id) }
        holder.binding.btnMap.setOnClickListener { onViewOnMap(hive) }
        holder.binding.root.setOnClickListener { onEdit(hive) }
    }

    override fun getItemCount() = hives.size

    fun updateData(newList: List<Hive>) {
        hives = newList
        notifyDataSetChanged()
    }
}
