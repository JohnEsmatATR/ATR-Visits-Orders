package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder
import com.google.android.material.card.MaterialCardView

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

        val item = mList[position]

        holder.tvCustomerName.text = item.CUSTOMER_NAME
        holder.tvSiteAddress.text = item.SITE_ADDRESS
        holder.tvWith.visibility = View.GONE

        if (item.isSelected) {
            holder.cardRoot.strokeWidth = 2.dp(holder.itemView.context)
            holder.cardRoot.strokeColor =
                ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary)
        } else {
            holder.cardRoot.strokeWidth = 0
        }
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

        val tvWith: TextView =
            itemView.findViewById(R.id.tvWith)

        val cardRoot: MaterialCardView =
            itemView.findViewById(R.id.cardRoot)

        init {

            itemView.setOnClickListener {

                val position = position

                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                mList.forEach { it.isSelected = false }

                mList[position].isSelected = true

                notifyDataSetChanged()

                listener.onClick(mList[position])
            }
        }
    }

    fun Int.dp(context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            context.resources.displayMetrics
        ).toInt()

    interface OnItemClickListener {
        fun onClick(item: CustomerAddres)
    }
}