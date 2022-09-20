package com.akhnaton.foodvisits.ui.orderHistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.orderHistory.OrderDetails
import com.akhnaton.foodvisits.databinding.ListOrderDetailsBinding

class OrderDetailsAdapter : RecyclerView.Adapter<OrderDetailsViewHolder>() {

    private var mOrder = mutableListOf<OrderDetails>()

    fun setOrderList(mOrder: List<OrderDetails>) {
        this.mOrder = mOrder.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListOrderDetailsBinding.inflate(inflater, parent, false)
        return OrderDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        holder.bind(mOrder[position])
    }

    override fun getItemCount(): Int {
        return mOrder.size
    }
}

class OrderDetailsViewHolder(val binding: ListOrderDetailsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(data: OrderDetails) {
        binding.order = data
        binding.executePendingBindings()
    }


}