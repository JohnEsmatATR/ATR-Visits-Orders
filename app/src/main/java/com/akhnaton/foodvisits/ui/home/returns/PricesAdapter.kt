package com.akhnaton.foodvisits.ui.home.returns

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getItemDetailsReturn.PRICES
import com.akhnaton.foodvisits.databinding.ItemPriceBinding

class PricesAdapter(
    private val prices: MutableList<PRICES>,
    private val listener: OnPriceSelected
) : RecyclerView.Adapter<PricesAdapter.ViewHolder>() {

    interface OnPriceSelected {
        fun onPriceSelected(price: PRICES)
    }

    inner class ViewHolder(
        private val binding: ItemPriceBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemPriceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = prices.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = prices[position]

        with(holder.itemView) {

            val binding = ItemPriceBinding.bind(this)

            binding.rbPrice.text =
                "${item.OPERAND}    (${item.CUST_PRICE} ${context.getString(R.string.currency)})"

            binding.rbPrice.isChecked = item.isSelected

            binding.rbPrice.setOnClickListener {

                prices.forEach { it.isSelected = false }

                item.isSelected = true

                notifyDataSetChanged()

                listener.onPriceSelected(item)
            }
        }
    }
}