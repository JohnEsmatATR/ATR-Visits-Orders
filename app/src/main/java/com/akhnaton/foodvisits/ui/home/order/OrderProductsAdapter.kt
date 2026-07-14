package com.akhnaton.foodvisits.ui.home.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.databinding.ItemProductBinding
import com.akhnaton.foodvisits.databinding.ItemProductBottomSheetBinding

class OrderProductsAdapter(
    private val list: MutableList<Product>
) : RecyclerView.Adapter<OrderProductsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.tvProductName.text = item.PRODUCT_NAME

        holder.binding.tvQuantity.text =
            "الكمية: ${item.selectedQty}"

        holder.binding.tvCustPrice.text =
            "الجمهور: ${item.CUST_PRICE}"

        holder.binding.tvPrice.text =
            "السعر: ${item.CUST_PRICE}"

        holder.binding.tvItemCode.text =
            "الكود: ${item.CUST_PRICE}"

        holder.binding.tvSegment2.text =
            "${item.SEGMENT2}"

        val totalPrice =
            "${holder.binding.root.context.getString(R.string.total)}: %.2f".format(item.selectedQty * item.ITEM_PRICE.toDouble())

        holder.binding.tvTotalPrice.text =
            "${totalPrice} ${holder.binding.root.context.getString(R.string.currency)}"

        holder.binding.tvTotalPrice.text =
            "${item.SEGMENT2}"

        holder.binding.tvPrice.text =
            (item.CUST_PRICE.toDouble() * item.selectedQty).toString()

    }

}