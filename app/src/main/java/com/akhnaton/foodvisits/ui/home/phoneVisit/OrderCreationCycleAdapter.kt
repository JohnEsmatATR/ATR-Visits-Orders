package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getStartOrderData.PaymentTerm
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectItem
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrderDetails
import com.akhnaton.foodvisits.ui.home.visits.order.OrderViewHolder

class OrderCreationCycleAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<OrderCreationCycleAdapter.ViewHolder>() {

    private var mList: List<SelectLists> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_order_option,
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
        holder.tvTitle.text =
            if (item.required == 1)
                "* ${item.select_name}"
            else
                item.select_name

        val dropdownItems =
            item.select_list.map {
                when {
                    it is PaymentTerm ->
                        it.PAYMENT_TERM_DESC

                    it is SelectItem ->
                        it.name

                    else ->
                        ""
                }
            }

        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_list_item_1,
            dropdownItems
        )

        holder.etOption.setAdapter(adapter)

        holder.etOption.setOnItemClickListener { _, _, index, _ ->

            item.selectedValue =
                dropdownItems[index]

            listener.onClick(item)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setList(list: List<SelectLists>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvTitle: TextView =
            itemView.findViewById(R.id.tvTitle)
        val etOption: AutoCompleteTextView =
            itemView.findViewById(R.id.etOption)

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
        fun onClick(item: SelectLists)
    }
}