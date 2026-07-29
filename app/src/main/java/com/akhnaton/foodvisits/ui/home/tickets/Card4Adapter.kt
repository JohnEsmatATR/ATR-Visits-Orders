package com.akhnaton.foodvisits.ui.home.tickets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder

class Card4Adapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<Card4Adapter.ViewHolder>() {

    private val mList = mutableListOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_card4,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.tvName.text = mList[position]
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setList(list: List<String>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val ivClose: ImageView =
            itemView.findViewById(R.id.ivClose)

        val tvName: TextView =
            itemView.findViewById(R.id.tvName)

        init {

            ivClose.setOnClickListener {

                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    listener.onClose(
                        mList[position]
                    )
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClose(item: String)
    }
}