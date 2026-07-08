package com.akhnaton.foodvisits.ui.home.visits2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getVisitPlan.CustomerVisitPlan
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder

class Visits2Adapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<Visits2Adapter.ViewHolder>() {

    private var mList: List<CustomerVisitPlan> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_card2,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.tvCustomerName.text = mList[position].customer_name
        holder.tvSiteAddress.text = mList[position].customer_address
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setList(list: List<CustomerVisitPlan>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvCustomerName: TextView =
            itemView.findViewById(R.id.tvCustomerName)

        val tvSiteAddress: TextView =
            itemView.findViewById(R.id.tvSiteAddress)

        val ivLocation: ImageView =
            itemView.findViewById(R.id.ivLocation)

        init {

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onClick(
                        mList[position]
                    )
                }
            }

            ivLocation.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLocationClick(mList[position])
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(item: CustomerVisitPlan)
        fun onLocationClick(item: CustomerVisitPlan)

    }
}