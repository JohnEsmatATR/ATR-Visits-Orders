package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder

class CustomerDataAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CustomerDataAdapter.ViewHolder>() {

    private var mList: List<CustomerAddres> = emptyList()

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
        holder.tvCustomerName.text = mList[position].CUSTOMER_NAME
        holder.tvSiteAddress.text = mList[position].SITE_ADDRESS
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setList(list: List<CustomerAddres>) {
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

        init {

            itemView.setOnClickListener {

                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    listener.onClick(
                        mList[position]
                    )
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(item: CustomerAddres)
    }
}