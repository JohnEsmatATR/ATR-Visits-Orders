package com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails

class OrderDetailsAdapter : RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder?>() {
    private var mList: List<SuperOrderDetails> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_order_details_super, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (item_description, order_quantity, price, tax, total_items, _, _, _, _, inventory_item_id) = mList[position]
        holder.title.text = item_description
        holder.qun.text = inventory_item_id
        holder.price.text = price
        holder.tax.text = tax
        holder.quantity.text = order_quantity
        holder.total.text = total_items + "EGP"
    }

    fun setList(orders: List<SuperOrderDetails>) {
        mList = orders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var qun: TextView
        var price: TextView
        var tax: TextView
        var total: TextView
        var quantity: TextView

        init {
            title = itemView.findViewById<View>(R.id.productName) as TextView
            qun = itemView.findViewById<View>(R.id.itemCode) as TextView
            price = itemView.findViewById<View>(R.id.price) as TextView
            tax = itemView.findViewById<View>(R.id.taxValue) as TextView
            total = itemView.findViewById<View>(R.id.total) as TextView
            quantity = itemView.findViewById<View>(R.id.quantity) as TextView
        }
    }

    interface OnOrderListener {
        fun onRejectClickListener(orderNumber: String?)
        fun onOrderClickListener(orderNumber: String?)
    }
}