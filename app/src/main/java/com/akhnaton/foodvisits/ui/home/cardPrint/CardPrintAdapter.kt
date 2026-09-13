package com.akhnaton.foodvisits.ui.home.cardPrint

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintItem
import com.akhnaton.foodvisits.databinding.CardPrintListBinding

class CardPrintAdapter(
    private var list: List<CardPrintItem>,
    private val onItemClick: (CardPrintItem) -> Unit
) : RecyclerView.Adapter<CardPrintAdapter.ViewHolder>() {

    class ViewHolder(val binding: CardPrintListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardPrintListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.cardPrint = list[position]
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            onItemClick(list[position])
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<CardPrintItem>) {
        list = newList
        notifyDataSetChanged()
    }

}