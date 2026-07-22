package com.akhnaton.foodvisits.ui.home.visits2

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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

        val item = mList[position]

        holder.tvCustomerName.text = item.customer_name
        holder.tvSiteAddress.text = item.customer_address
        holder.tvWith.text = "المرافق: ${item.visit_with_name}"

        if (item.visit_with_name == null || item.visit_with_name == "") {
            holder.tvWith.visibility = View.GONE
        }

        if (item.is_visited_today) {

            holder.imgArrow.visibility = View.GONE
            holder.tvVisited.visibility = View.VISIBLE

            // Strike through text
            holder.tvCustomerName.paintFlags =
                holder.tvCustomerName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            holder.tvSiteAddress.paintFlags =
                holder.tvSiteAddress.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // Optional: gray text
            holder.tvCustomerName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.grey_color)
            )

            holder.tvSiteAddress.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.grey_color)
            )

            // Green location icon background
            holder.ivLocation.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )

        } else {

            holder.imgArrow.visibility = View.VISIBLE
            holder.tvVisited.visibility = View.GONE

            // Remove strike through
            holder.tvCustomerName.paintFlags =
                holder.tvCustomerName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.tvSiteAddress.paintFlags =
                holder.tvSiteAddress.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // Restore colors
            holder.tvCustomerName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.black)
            )

            holder.tvSiteAddress.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.grey_color)
            )

            // Restore original background
            holder.ivLocation.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary)
            )
        }
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

        val tvWith: TextView =
            itemView.findViewById(R.id.tvWith)

        val ivLocation: ImageView =
            itemView.findViewById(R.id.ivLocation)

        val imgArrow: ImageView =
            itemView.findViewById(R.id.imgArrow)

        val tvVisited: TextView =
            itemView.findViewById(R.id.tvVisited)

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