package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisit

class BundleVisitsAdapter(
    private val onItemClick: (BundleVisit) -> Unit
) : RecyclerView.Adapter<BundleVisitsAdapter.ViewHolder>() {

    private var mList: List<BundleVisit> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bundle_visit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.tvCustomerName.text = item.customerName
        holder.tvSiteAddress.text = item.customerAddress.trim()
        holder.tvCustomerCode.text = "كود: ${item.customerCode}"
        holder.tvOrderType.text = item.customerOrderType
    //    holder.tvVisitsCount.text = "الزيارات: ${item.countOfVisits}"
    }

    override fun getItemCount() = mList.size

    fun setList(list: List<BundleVisit>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvSiteAddress: TextView = itemView.findViewById(R.id.tvSiteAddress)
        val tvCustomerCode: TextView = itemView.findViewById(R.id.tvCustomerCode)
        val tvOrderType: TextView = itemView.findViewById(R.id.tvOrderType)
     //   val tvVisitsCount: TextView = itemView.findViewById(R.id.tvVisitsCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(mList[position])
                }
            }
        }
    }
}