package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getList.ITEMSPREVIEW

class ProductPreviewAdapter(
    private val list: List<ITEMSPREVIEW>
) : RecyclerView.Adapter<ProductPreviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_product_preview,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.tvName.text =
            list[position].NAME

        holder.tvQty.text =
            list[position].QUANTITY.toString()
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvName =
            view.findViewById<TextView>(R.id.tvName)

        val tvQty =
            view.findViewById<TextView>(R.id.tvQty)
    }
}