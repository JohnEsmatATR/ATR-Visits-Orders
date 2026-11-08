package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisit

class BundleVisitsAdapter(
    private val onItemClick: (BundleVisit) -> Unit
) : RecyclerView.Adapter<BundleVisitsAdapter.ViewHolder>() {

    private var mList: List<BundleVisit> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_bundle_visit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.tvCustomerName.text = item.custName
        holder.tvSiteAddress.text = item.custAddress.trim()
        holder.tvCustomerCode.text = "كود: ${item.custId}"
        holder.tvVisitDate.text = item.visitDate
        holder.tvStatus.text =
            if (item.status == 0) holder.itemView.context.getString(R.string.pending)
            else if (item.status == 1) holder.itemView.context.getString(R.string.done)
            else holder.itemView.context.getString(R.string.reject)
        if (item.status == 1 || item.status == 2) {
            holder.imgNav.visibility = View.GONE
            holder.imgLock.visibility = View.VISIBLE
        } else {
            holder.imgNav.visibility = View.VISIBLE
            holder.imgLock.visibility = View.GONE
        }
        when (item.status) {
            0 -> {
                holder.tvStatus.text = holder.itemView.context.getString(R.string.pending)
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.colorAccent
                    )
                )
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_orange_light)
                holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_dot_orange, 0, 0, 0
                )
            }

            1 -> {
                holder.tvStatus.text = holder.itemView.context.getString(R.string.done)
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.green2
                    )
                )
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_green_light)
                holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_dot_green, 0, 0, 0
                )
            }

            2 -> {
                holder.tvStatus.text = holder.itemView.context.getString(R.string.reject)
                holder.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.red
                    )
                )
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_red_light)
                holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_dot_red, 0, 0, 0
                )
            }
        }
        //    holder.tvVisitsCount.text = "الزيارات: ${item.countOfVisits}"
    }

    override fun getItemCount() = mList.size

    fun setList(list: List<BundleVisit>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgLock: ImageView = itemView.findViewById(R.id.imgLock)
        val imgNav: ImageView = itemView.findViewById(R.id.imgNav)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvSiteAddress: TextView = itemView.findViewById(R.id.tvSiteAddress)
        val tvCustomerCode: TextView = itemView.findViewById(R.id.tvCustomerCode)
        val tvVisitDate: TextView = itemView.findViewById(R.id.tvVisitDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        //   val tvVisitsCount: TextView = itemView.findViewById(R.id.tvVisitsCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && mList[position].status == 0) {
                    onItemClick(mList[position])
                }
            }
        }
    }
}