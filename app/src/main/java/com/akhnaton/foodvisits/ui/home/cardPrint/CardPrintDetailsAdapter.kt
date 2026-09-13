package com.akhnaton.foodvisits.ui.home.cardPrint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.cardPrint.InvoiceDetailItem
import com.akhnaton.foodvisits.databinding.ItemInvoiceDetailBinding

class CardPrintDetailsAdapter(
    private var items: List<InvoiceDetailItem>
) : RecyclerView.Adapter<CardPrintDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemInvoiceDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = items[position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<InvoiceDetailItem>) {
        items = ArrayList(newItems)
        notifyDataSetChanged()
    }
}