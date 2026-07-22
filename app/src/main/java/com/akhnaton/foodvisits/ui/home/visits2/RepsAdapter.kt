package com.akhnaton.foodvisits.ui.home.visits2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.getSalesMan.SalesMan
import com.akhnaton.foodvisits.databinding.ItemRepBinding

class RepsAdapter(
    private val list: MutableList<SalesMan>
) : RecyclerView.Adapter<RepsAdapter.ViewHolder>() {

    var selectedPosition = -1

    inner class ViewHolder(val binding: ItemRepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemRepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.tvName.text = item.SALES_MAN
        holder.binding.tvCode.text = "الكود: ${item.PERSON_ID}"
        holder.binding.radioButton.isChecked = selectedPosition == position

        holder.itemView.setOnClickListener {
            holder.binding.radioButton.isChecked = true
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }

        holder.binding.radioButton.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }

    fun getSelected(): SalesMan? {

        return if (selectedPosition == -1)
            null
        else
            list[selectedPosition]
    }

    fun filter(newList: List<SalesMan>) {

        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}