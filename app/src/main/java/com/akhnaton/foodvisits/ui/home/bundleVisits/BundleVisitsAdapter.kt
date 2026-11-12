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
        val card = holder.itemView as com.google.android.material.card.MaterialCardView
        val ctx = holder.itemView.context
        val strokePx = (1 * ctx.resources.displayMetrics.density).toInt()
        val grey = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.grey_color))

        when (item.status) {
            1 -> {
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.card_done_bg))
                card.strokeColor = ContextCompat.getColor(ctx, R.color.card_done_stroke)
                card.strokeWidth = strokePx
                card.alpha = 0.6f
                holder.tvCustomerName.setTextColor(ContextCompat.getColor(ctx, R.color.grey_color))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_done)
                holder.flStatusCircle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.green))
            }
            2 -> {
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.card_reject_bg))
                card.strokeColor = ContextCompat.getColor(ctx, R.color.card_reject_stroke)
                card.strokeWidth = strokePx
                card.alpha = 0.6f
                holder.tvCustomerName.setTextColor(ContextCompat.getColor(ctx, R.color.grey_color))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_close_done)
                holder.flStatusCircle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.red))
            }
            else -> {
                card.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.white))
                card.strokeWidth = 0
                card.alpha = 1f
                holder.tvCustomerName.setTextColor(ContextCompat.getColor(ctx, R.color.black))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_bundle)
                holder.flStatusCircle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorPrimary))
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
        val flStatusCircle: android.widget.FrameLayout = itemView.findViewById(R.id.flStatusCircle)
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
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