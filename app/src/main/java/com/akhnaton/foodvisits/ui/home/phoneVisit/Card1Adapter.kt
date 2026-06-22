package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder

class Card1Adapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<Card1Adapter.ViewHolder>() {

    private var mList: List<String> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_card1,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.title.text = mList[position]
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setList(list: List<String>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val title: TextView =
            itemView.findViewById(R.id.tvTitle)

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
        fun onClick(item: String)
    }
}