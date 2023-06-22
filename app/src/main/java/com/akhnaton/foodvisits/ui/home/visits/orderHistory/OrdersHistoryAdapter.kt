package com.akhnaton.foodvisits.ui.home.visits.orderHistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistoryData
import com.akhnaton.foodvisits.databinding.ListOrderHistoryBinding
import com.akhnaton.foodvisits.shared.ConvertDate

class OrdersHistoryAdapter : RecyclerView.Adapter<OrdersHistoryViewHolder>() {

    private var mOrdersHistory = mutableListOf<OrderHistoryData>()
    private lateinit var listener: OrdersHistoryViewHolder.OnSelectOrderClickListener

    fun setOrdersList(
        ordersHistory: List<OrderHistoryData>,
        listener: OrdersHistoryViewHolder.OnSelectOrderClickListener
    ) {
        this.mOrdersHistory = ordersHistory.toMutableList()
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListOrderHistoryBinding.inflate(inflater, parent, false)
        return OrdersHistoryViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: OrdersHistoryViewHolder, position: Int) {
        holder.bind(mOrdersHistory[position])

        holder.binding.date.text = ConvertDate.getDateTime(mOrdersHistory[position].order_created_at.toLong())

        if (mOrdersHistory[position].flag == "1") {
            holder.binding.layoutItem.setBackgroundResource(R.color.order_card)
            holder.binding.orderMadeBy.text = "Confirmed"
        } else {
            holder.binding.layoutItem.setBackgroundResource(R.color.blue)
            holder.binding.orderMadeBy.text = "Pending"
        }
    }

    override fun getItemCount(): Int {
        return mOrdersHistory.size
    }
}

class OrdersHistoryViewHolder(
    val binding: ListOrderHistoryBinding,
    val listener: OnSelectOrderClickListener
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(data: OrderHistoryData) {
        binding.order = data
        binding.orderCard.setOnClickListener {
            listener.onSelectOrderClickListener(
                data,
                adapterPosition
            )
        }
        binding.executePendingBindings()
    }

    interface OnSelectOrderClickListener {
        fun onSelectOrderClickListener(data: OrderHistoryData, position: Int)
    }
}
