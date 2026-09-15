package com.akhnaton.foodvisits.ui.home.visitPlan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.Visit

class VisitAdapter(
    private val onDeleteClick: (Visit) -> Unit = {},
    private val onSwapClick: (Visit) -> Unit = {}
) : RecyclerView.Adapter<VisitAdapter.VisitViewHolder>() {

    private var items: List<Visit> = emptyList()

    fun submitList(newItems: List<Visit>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VisitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPharmacyName: TextView = view.findViewById(R.id.tv_pharmacy_name)
        val tvCodeLocation: TextView = view.findViewById(R.id.tv_code_location)
        val tvDelegate: TextView = view.findViewById(R.id.tv_delegate)
        val tvAddress: TextView = view.findViewById(R.id.tv_address)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val ivDelete: ImageView = view.findViewById(R.id.iv_delete)
        val ivSwap: ImageView = view.findViewById(R.id.iv_swap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visit_card, parent, false)
        return VisitViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        val visit = items[position]
        val context = holder.itemView.context

        holder.tvPharmacyName.text = visit.pharmacyName
        holder.tvCodeLocation.text =
            "${context.getString(R.string.code_label)}: ${visit.code} • ${context.getString(R.string.location_code_label)}: ${visit.locationCode}"
        holder.tvDelegate.text = "${context.getString(R.string.delegate_prefix)} ${visit.repName}"
        holder.tvAddress.text = visit.address

        if (visit.isApproved) {
            holder.tvStatus.text = context.getString(R.string.status_approved)
            val greenColor = ContextCompat.getColor(context, R.color.green)
            holder.tvStatus.setTextColor(greenColor)
            tintStatusDot(holder.tvStatus, greenColor)
        } else {
            holder.tvStatus.text = context.getString(R.string.status_unapproved)
            val orangeColor = ContextCompat.getColor(context, R.color.colorAccent)
            holder.tvStatus.setTextColor(orangeColor)
            tintStatusDot(holder.tvStatus, orangeColor)
        }

        holder.ivDelete.setOnClickListener { onDeleteClick(visit) }
        holder.ivSwap.setOnClickListener { onSwapClick(visit) }
    }

    private fun tintStatusDot(textView: TextView, color: Int) {
        val drawables = TextViewCompat.getCompoundDrawablesRelative(textView)
        val drawable = drawables[0] ?: return
        val wrapped = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapped, color)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            textView, wrapped, null, null, null
        )
    }
}