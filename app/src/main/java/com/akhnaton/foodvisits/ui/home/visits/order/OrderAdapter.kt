package com.akhnaton.foodvisits.ui.home.visits.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.databinding.ListNewOrderBinding

class OrderAdapter : RecyclerView.Adapter<OrderViewHolder>() {

    private var mProduct = mutableListOf<CardItem>()
    private lateinit var listener: OrderViewHolder.OnItemClickListener

    fun addProduct(mProduct: List<CardItem>) {
        this.mProduct = mProduct.toMutableList()
        notifyDataSetChanged()
    }

    fun deleteProduct(listener: OrderViewHolder.OnItemClickListener) {
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListNewOrderBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding,listener)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(mProduct[position])
    }

    override fun getItemCount(): Int {
        return mProduct.size
    }
}

class OrderViewHolder(val binding: ListNewOrderBinding, val listener: OnItemClickListener) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(data: CardItem) {
        binding.product = data
        binding.deleteSelectedItem.setOnClickListener {
            listener.onDeleteClick(data)
        }
        binding.executePendingBindings()
    }


    interface OnItemClickListener {
        fun onDeleteClick(item: CardItem)
    }
}