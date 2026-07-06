package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.customers.Data

class CustomersAdapter(
    private val list: List<Data>,
    private val listener: (Data) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_LOCKED = 0
        private const val TYPE_UNLOCKED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].SELECTABLE)
            TYPE_UNLOCKED
        else
            TYPE_LOCKED
    }

    class LockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryCode: TextView =
            view.findViewById(R.id.tvCategoryCode)

        val tvDisplayName: TextView =
            view.findViewById(R.id.tvDisplayName)

        val tvCustomerCodeCategoryCode: TextView =
            view.findViewById(R.id.tvCustomerCodeCategoryCode)
    }

    class UnlockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryCode: TextView =
            view.findViewById(R.id.tvCategoryCode)

        val tvDisplayName: TextView =
            view.findViewById(R.id.tvDisplayName)

        val tvCustomerCodeCategoryCode: TextView =
            view.findViewById(R.id.tvCustomerCodeCategoryCode)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_LOCKED) {

            LockedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_customer_unlocked,
//                        R.layout.item_customer_locked,
                        parent,
                        false
                    )
            )

        } else {

            UnlockedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_customer_unlocked,
                        parent,
                        false
                    )
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        val item = list[position]

        when (holder) {

            is LockedViewHolder -> {

                holder.tvCategoryCode.text =
                    item.CATEGORY_CODE

                holder.tvDisplayName.text =
                    item.DISPLAY_NAME

                holder.tvCustomerCodeCategoryCode.text =
                    "كود: ${item.CUSTOMER_CODE} • فئة: ${item.CATEGORY_CODE}"
            }

            is UnlockedViewHolder -> {

                holder.tvCategoryCode.text =
                    item.CATEGORY_CODE

                holder.tvDisplayName.text =
                    item.DISPLAY_NAME

                holder.tvCustomerCodeCategoryCode.text =
                    "كود: ${item.CUSTOMER_CODE} • فئة: ${item.CATEGORY_CODE}"
            }
        }

        holder.itemView.setOnClickListener {
            listener(item)
        }
    }

    override fun getItemCount(): Int = list.size

}