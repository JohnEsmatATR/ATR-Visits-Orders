package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getItems.Data
import com.akhnaton.foodvisits.data.model.getList.ITEMSPREVIEW

class ProductPreviewAdapter(
    private val list: List<Data>
) : RecyclerView.Adapter<ProductPreviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_product_preview2,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvItemName.text = item.ITEM_NAME
        holder.tvUnitPrice.text = "السعر: ${item.UNIT_PRICE}"
        holder.tvTax.text = "الضريبة: ${item.TAX}"
        holder.tvQuantity.text = "الكمية: ${item.QUANTITY}"
        holder.tvTotalValue.text = "القيمة الإجمالية: ${item.TOTAL_VALUE} ج.م"

        holder.tvBackOrder.visibility =
            if (item.IS_BACK_ORDER) View.VISIBLE else View.GONE
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvItemName =
            view.findViewById<TextView>(R.id.tvItemName)

        val tvQuantity =
            view.findViewById<TextView>(R.id.tvQuantity)

        val tvTax =
            view.findViewById<TextView>(R.id.tvTax)

        val tvUnitPrice =
            view.findViewById<TextView>(R.id.tvUnitPrice)

        val tvTotalValue =
            view.findViewById<TextView>(R.id.tvTotalValue)

        val tvBackOrder =
            view.findViewById<TextView>(R.id.tvBackOrder)
    }
}