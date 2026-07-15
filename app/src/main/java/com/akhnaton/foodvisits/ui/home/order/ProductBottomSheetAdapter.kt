package com.akhnaton.foodvisits.ui.home.order

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.databinding.ItemProductBottomSheetBinding

class ProductBottomSheetAdapter(
    private val list: MutableList<Product>, private val click: (Product) -> Unit
) : RecyclerView.Adapter<ProductBottomSheetAdapter.Holder>(), Filterable {

    private var filtered = list.toMutableList()

    inner class Holder(val binding: ItemProductBottomSheetBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            ItemProductBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount() = filtered.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = filtered[position]
        holder.binding.tvProductName.text = item.PRODUCT_NAME
        holder.binding.tvQuota.text = if (item.MANDATORY_QOUTA == "1") {
            holder.binding.tvQuota.setTextColor(holder.binding.root.resources.getColor(R.color.red))
            "كوتة إجبارية : ${item.QUOTA_QTY}"
        } else {
            holder.binding.tvQuota.setTextColor(holder.binding.root.resources.getColor(R.color.green))
            "كوتة : ${item.QUOTA_QTY}"
        }
        holder.binding.tvCategory.text = item.SEGMENT2
        holder.binding.tvPrice.text = "السعر : ${item.CUST_PRICE}"
        holder.binding.tvCustPrice.text = "جمهور : ${item.ITEM_PRICE}"
        holder.binding.tvItemCode.text = "كود : ${item.ITEM_CODE}"
        holder.itemView.setOnClickListener {
            click(item)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence.toString()
                filtered = if (query.isEmpty()) {
                    list.toMutableList()
                } else {
                    list.filter {
                        it.PRODUCT_NAME.contains(query, true) || it.ITEM_CODE.contains(query, true)
                    }.toMutableList()
                }

                return FilterResults().apply {
                    values = filtered
                }
            }

            override fun publishResults(
                charSequence: CharSequence?, results: FilterResults?
            ) {
                filtered = results?.values as MutableList<Product>
                notifyDataSetChanged()
            }
        }
    }
}