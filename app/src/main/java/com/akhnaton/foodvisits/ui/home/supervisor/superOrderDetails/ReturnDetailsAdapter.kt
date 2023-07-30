package com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperReturnDetails


class ReturnDetailsAdapter : RecyclerView.Adapter<ReturnDetailsAdapter.ViewHolder?>() {
    private var mList: List<SuperReturnDetails> = ArrayList<SuperReturnDetails>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_order_details_super, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order: SuperReturnDetails = mList[position]
        holder.title.setText(order.item_description)
        holder.price.setText(order.price)
        holder.tax.setText(order.tax)
        holder.quantity.setText(order.order_quantity)
        holder.total.setText(order.total_items + "EGP")
        holder.qun.setVisibility(View.GONE)
        holder.itemCodeText.setVisibility(View.GONE)
    }

    fun setList(orders: List<SuperReturnDetails>) {
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
        var itemCodeText: TextView

        init {
            title = itemView.findViewById<View>(R.id.productName) as TextView
            qun = itemView.findViewById<View>(R.id.itemCode) as TextView
            itemCodeText = itemView.findViewById<View>(R.id.itemCodeText) as TextView
            price = itemView.findViewById<View>(R.id.price) as TextView
            tax = itemView.findViewById<View>(R.id.taxValue) as TextView
            total = itemView.findViewById<View>(R.id.total) as TextView
            quantity = itemView.findViewById<View>(R.id.quantity) as TextView
        }
    }
}