package com.akhnaton.foodvisits.ui.home.visitPlan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visitPlan.VisitItem
import com.akhnaton.foodvisits.databinding.ItemVisitCardBinding
import androidx.core.content.ContextCompat

class VisitsAdapter(
    private var list: List<VisitItem>,
    private val onItemClick: (VisitItem) -> Unit,
    private val onSwapClick: (VisitItem) -> Unit
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
            tvPharmacyName.text = item.customer_name
            tvCodeLocation.text = "كود: ${item.customer_code} • موقع: ${item.party_site}"
            tvDelegate.text = "المندوب: ${item.sales_man}"
            tvAddress.text = item.site_address

            when (item.approve) {
                "1" -> {
                    tvStatus.text = "معتمدة"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.green2))
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_green_light)
                    tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_dot_green, 0, 0, 0
                    )
                }
                "" -> {
                    tvStatus.text = "غير معتمدة"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.colorAccent))
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_orange_light)
                    tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_dot_orange, 0, 0, 0
                    )
                }
                else -> {
                    tvStatus.text = "قيد الانتظار"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_gray_light)
                    tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_dot_gray, 0, 0, 0
                    )
                }
            }

            ivSwap.setOnClickListener {
                onSwapClick(item)
            }
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