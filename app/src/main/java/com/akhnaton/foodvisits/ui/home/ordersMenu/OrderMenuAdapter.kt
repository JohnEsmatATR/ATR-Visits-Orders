package com.akhnaton.foodvisits.ui.home.order.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getList.Data

class OrderMenuAdapter(
    private val list: MutableList<Data>,
    private val listener: OrderActionListener
) : RecyclerView.Adapter<OrderMenuAdapter.ViewHolder>() {

    private val expandedItems = HashSet<Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_orders_menu,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.tvPharmacyName.text = list[position].CUSTOMER_NAME
        holder.tvStatus.text = if (list[position].ORDER_STATUS == "saved") {
            holder.itemView.context.getString(R.string.saved)
        } else if (list[position].ORDER_STATUS == "sent") {
            holder.itemView.context.getString(R.string.sent)
        } else {
            ""
        }
        holder.tvInfo.text = "${holder.itemView.context.getString(R.string.code)}: " +
                "${list[position].CUSTOMER_CODE} • " +
                "${holder.itemView.context.getString(R.string.invoice)}: " +
                "${list[position].ORIG_SYS_DOCUMENT_REF}"
        holder.tvDate.text = list[position].ORDER_DATE
        holder.tvItems.text = "${list[position].ITEMS_COUNT} " +
                "${holder.itemView.context.getString(R.string.product)} " +
                "(${list[position].TOTAL_QUANTITY} " +
                "${holder.itemView.context.getString(R.string.piece)})"
        holder.tvAmount.text =
            "${list[position].TOTAL_VALUE} ${holder.itemView.context.getString(R.string.currency)}"
        holder.tvMoreProducts.text = "+${list[position].ITEMS_COUNT.toInt() - 3}"
        val expanded =
            expandedItems.contains(position)

        holder.layoutExpanded.visibility =
            if (expanded) View.VISIBLE else View.GONE

        holder.layoutItemsContainer.removeAllViews()

        val items = list[position].ITEMS_PREVIEW

        val previewItems =
            if (items.size > 3)
                items.take(3)
            else
                items

        previewItems.forEach { product ->

            val itemView = LayoutInflater
                .from(holder.itemView.context)
                .inflate(
                    R.layout.item_product_preview,
                    holder.layoutItemsContainer,
                    false
                )

            itemView.findViewById<TextView>(R.id.tvName).text =
                product.NAME

            itemView.findViewById<TextView>(R.id.tvQty).text =
                "الكمية: ${product.QUANTITY}"

            holder.layoutItemsContainer.addView(itemView)
        }

        holder.itemView.setOnClickListener {

            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }

            androidx.transition.TransitionManager.beginDelayedTransition(
                holder.itemView.parent as ViewGroup
            )

            notifyItemChanged(position)
        }

        if (list[position].ORDER_STATUS == "saved") {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
//            holder.btnDetails.visibility = View.GONE
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }

        holder.layoutMoreDetails.visibility =
            if (
                list[position].ORDER_STATUS == "sent" &&
                list[position].ITEMS_COUNT.toInt() > 3
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.btnEdit.setOnClickListener {
            listener.onEdit(list[position])
        }

        holder.btnDelete.setOnClickListener {
            listener.onDelete(list[position])
        }

        holder.btnDetails.setOnClickListener {
            listener.onShowDetails(list[position])
        }

        holder.layoutMoreDetails.setOnClickListener {
            listener.onShowDetails(list[position])
        }
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvPharmacyName: TextView =
            itemView.findViewById(R.id.tvPharmacyName)

        val tvStatus: TextView =
            itemView.findViewById(R.id.tvStatus)

        val tvInfo: TextView =
            itemView.findViewById(R.id.tvInfo)

        val tvDate: TextView =
            itemView.findViewById(R.id.tvDate)

        val tvItems: TextView =
            itemView.findViewById(R.id.tvItems)

        val tvAmount: TextView =
            itemView.findViewById(R.id.tvAmount)

        val tvMoreProducts: TextView =
            itemView.findViewById(R.id.tvMoreProducts)

        val layoutExpanded =
            itemView.findViewById<LinearLayout>(R.id.layoutExpanded)

        val layoutItemsContainer =
            itemView.findViewById<LinearLayout>(R.id.layoutItemsContainer)

        val btnEdit =
            itemView.findViewById<Button>(R.id.btnEdit)

        val btnDelete =
            itemView.findViewById<Button>(R.id.btnDelete)

        val btnDetails =
            itemView.findViewById<TextView>(R.id.btnDetails)

        val layoutMoreDetails =
            itemView.findViewById<LinearLayout>(R.id.layoutMoreDetails)
    }

    interface OrderActionListener {
        fun onEdit(order: Data)
        fun onDelete(order: Data)
        fun onShowDetails(order: Data)
    }
}