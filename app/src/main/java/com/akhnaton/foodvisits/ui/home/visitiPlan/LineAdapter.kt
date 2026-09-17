package com.akhnaton.foodvisits.ui.home.visitPlan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.visitPlan.LineItem
import com.akhnaton.foodvisits.databinding.ItemRouteOptionBinding

class LineAdapter(
    private var items: List<LineItem>,
    private val getSelectedCode: () -> String?,
    private val onLineClick: (LineItem) -> Unit
) : RecyclerView.Adapter<LineAdapter.LineViewHolder>() {

    inner class LineViewHolder(val binding: ItemRouteOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val binding = ItemRouteOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.tvRouteName.text = item.LINE_NAME
        binding.tvRouteCode.text = "المخزن: ${item.STORE_CODE}  •  كود: ${item.LINE_CODE}"

        val isSelected = item.LINE_CODE == getSelectedCode()

        if (isSelected) {
            binding.cardRouteItem.setCardBackgroundColor(Color.parseColor("#FFF5EC"))
            binding.ivRouteCheck.visibility = View.VISIBLE
            binding.ivRouteIconBg.setCardBackgroundColor(Color.parseColor("#FF8A00"))
            binding.ivRouteIcon.setColorFilter(Color.WHITE)
        } else {
            binding.cardRouteItem.setCardBackgroundColor(Color.parseColor("#FAFAFA"))
            binding.ivRouteCheck.visibility = View.GONE
            binding.ivRouteIconBg.setCardBackgroundColor(Color.parseColor("#FFEEDD"))
            binding.ivRouteIcon.setColorFilter(Color.parseColor("#FF8A00"))
        }

        binding.root.setOnClickListener {
            onLineClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<LineItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}