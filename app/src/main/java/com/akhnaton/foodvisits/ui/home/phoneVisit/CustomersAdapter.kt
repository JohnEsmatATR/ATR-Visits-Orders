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

        val tvCustomerCode: TextView =
            view.findViewById(R.id.tvCustomerCode)

        val tvCategoryMeaning: TextView =
            view.findViewById(R.id.tvCategoryMeaning)

        val tvTeamName: TextView =
            view.findViewById(R.id.tvTeamName)

        val tvCustomerProfileDesc: TextView =
            view.findViewById(R.id.tvCustomerProfileDesc)
    }

    class UnlockedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryCode: TextView =
            view.findViewById(R.id.tvCategoryCode)

        val tvDisplayName: TextView =
            view.findViewById(R.id.tvDisplayName)

        val tvCustomerCode: TextView =
            view.findViewById(R.id.tvCustomerCode)

        val tvCategoryMeaning: TextView =
            view.findViewById(R.id.tvCategoryMeaning)

        val tvTeamName: TextView =
            view.findViewById(R.id.tvTeamName)

        val tvCustomerProfileDesc: TextView =
            view.findViewById(R.id.tvCustomerProfileDesc)
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

        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams

        params.bottomMargin =
            if (position == list.lastIndex) {
                holder.itemView.context.resources
                    .getDimensionPixelSize(com.intuit.sdp.R.dimen._86sdp)
            } else {
                0
            }

        val item = list[position]

        when (holder) {

            is LockedViewHolder -> {

                holder.tvCategoryCode.text =
                    item.CATEGORY_CODE

                holder.tvDisplayName.text =
                    item.DISPLAY_NAME

                holder.tvCustomerCode.text =
                    "الكود : ${item.CUSTOMER_CODE}"

                holder.tvCategoryMeaning.text =
                    "الفئة : ${item.CATEGORY_MEANING}"

                holder.tvTeamName.text =
                    "الخط : ${item.TEAM_NAME}"

                holder.tvCustomerProfileDesc.text =
                    "نوع العميل : ${item.CUSTOMER_PROFILE_DESC}"
            }

            is UnlockedViewHolder -> {

                holder.tvCategoryCode.text =
                    item.CATEGORY_CODE

                holder.tvDisplayName.text =
                    item.DISPLAY_NAME

                holder.tvCustomerCode.text =
                    "الكود : ${item.CUSTOMER_CODE}"

                holder.tvCategoryMeaning.text =
                    "الفئة : ${item.CATEGORY_MEANING}"

                holder.tvTeamName.text =
                    "الخط : ${item.TEAM_NAME}"

                holder.tvCustomerProfileDesc.text =
                    "نوع العميل : ${item.CUSTOMER_PROFILE_DESC}"
            }
        }

        holder.itemView.setOnClickListener {
            listener(item)
        }
    }

    override fun getItemCount(): Int = list.size

}