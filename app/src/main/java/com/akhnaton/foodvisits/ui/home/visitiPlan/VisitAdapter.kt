package com.akhnaton.foodvisits.ui.home.visitPlan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.visitPlan.VisitItem
import com.akhnaton.foodvisits.databinding.ItemVisitCardBinding

class VisitsAdapter(
    private var list: List<VisitItem>,
    private val onItemClick: (VisitItem) -> Unit
) : RecyclerView.Adapter<VisitsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemVisitCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVisitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvPharmacyName.text = item.title
            tvCodeLocation.text = "كود: ${item.id} • موقع: ${item.party_site}"

            if (item.approve.isNullOrEmpty()) {
                tvStatus.text = "غير معتمدة"
            } else {
                tvStatus.text = "معتمدة"
            }

            tvDelegate.visibility = View.GONE
            tvAddress.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<VisitItem>) {
        list = newList
        notifyDataSetChanged()
    }

}