package com.akhnaton.foodvisits.ui.home.order

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists

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
        holder.tvTitle.text = item.select_name
        if (item.required == 1)
            holder.tvReq.visibility = View.VISIBLE
        else
            holder.tvReq.visibility = View.GONE

        Log.d("DROPDOWN", "========================")
        Log.d("DROPDOWN", "Title: ${item.select_name}")
        Log.d("DROPDOWN", "Required: ${item.required}")
        Log.d("DROPDOWN", "Select: ${item.select}")

        item.select_list.forEachIndexed { index, option ->
            Log.d("DROPDOWN", "Item[$index] = $option")
        }

        val dropdownItems =
            item.select_list.map {
                when {
                    it.PAYMENT_TERM_DESC != null ->
                        it.PAYMENT_TERM_DESC

                    it.name != null ->
                        it.name

                    else ->
                        ""
                }
            }

        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_dropdown_item_1line,
            dropdownItems
        )

        holder.etOption.setAdapter(adapter)

        holder.etOption.setOnItemClickListener { _, _, index, _ ->

            val selectedItem =
                item.select_list[index]

            when {
                selectedItem.PAYMENT_TERM_ID != null -> {

                    item.selectedId =
                        selectedItem.PAYMENT_TERM_ID

                    item.selectedValue =
                        selectedItem.PAYMENT_TERM_DESC
                }

                selectedItem.id != null -> {

                    item.selectedId =
                        selectedItem.id.toString()

                    item.selectedValue =
                        selectedItem.name
                }
            }

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

        val tvReq: TextView =
            itemView.findViewById(R.id.tvReq)
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