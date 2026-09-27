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
    private val onSwapClick: (VisitItem) -> Unit,
    private val onDeleteClick: (VisitItem) -> Unit,
    private val onSelectToggle: (VisitItem) -> Unit
) : RecyclerView.Adapter<VisitsAdapter.ViewHolder>() {

    private var actionsVisible = true
    private var selectionMode = false
    private var selectedIds: Set<String> = emptySet()

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
                "0" -> {
                    tvStatus.text = "مرفوضه"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.gray))
                    tvStatus.setBackgroundResource(R.drawable.bg_chip_gray_light)
                    tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_dot_gray, 0, 0, 0
                    )
                }
            }

            val actionsVisibility = if (actionsVisible && !selectionMode) View.VISIBLE else View.GONE
            ivDelete.visibility = actionsVisibility
            ivSwap.visibility = actionsVisibility

            ivSelectCircle.visibility = if (selectionMode) View.VISIBLE else View.GONE
            ivSelectCircle.setImageResource(
                if (selectedIds.contains(item.id)) R.drawable.ic_check_circle_orange
                else R.drawable.ic_circle_unchecked
            )

            ivDelete.setOnClickListener {
                onDeleteClick(item)
            }
            ivSwap.setOnClickListener {
                onSwapClick(item)
            }
            ivSelectCircle.setOnClickListener {
                onSelectToggle(item)
            }
        }

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                onSelectToggle(item)
            } else {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun setActionsVisible(visible: Boolean) {
        if (actionsVisible != visible) {
            actionsVisible = visible
            notifyDataSetChanged()
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        if (selectionMode != enabled) {
            selectionMode = enabled
            notifyDataSetChanged()
        }
    }

    fun setSelectedIds(ids: Set<String>) {
        selectedIds = ids
        notifyDataSetChanged()
    }

    fun updateList(newList: List<VisitItem>) {
        list = newList
        notifyDataSetChanged()
    }
}