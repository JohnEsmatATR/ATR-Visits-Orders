package com.akhnaton.foodvisits.ui.orderHistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.model.VisitsPlaneDataDumy
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistory
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistoryData
import com.akhnaton.foodvisits.data.model.visits.CustomerSiteData
import com.akhnaton.foodvisits.databinding.ListOrderHistoryBinding
import com.akhnaton.foodvisits.databinding.ListPlanBinding
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
