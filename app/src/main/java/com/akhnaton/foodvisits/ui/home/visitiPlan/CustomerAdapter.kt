package com.akhnaton.foodvisits.ui.home.visitPlan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.visitPlan.CustomerItem
import com.akhnaton.foodvisits.databinding.ItemCustomerOptionBinding

class CustomersAdapter(
    private var list: List<CustomerItem>,
    private val isSelected: (CustomerItem) -> Boolean,
    private val onToggle: (CustomerItem) -> Unit
) : RecyclerView.Adapter<CustomersAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCustomerOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvCustomerName.text = item.CUSTOMER_NAME
            tvCustomerAddress.text = "${item.SITE_ADDRESS} • كود: ${item.CUSTOMER_CODE}"
            cbCustomer.isChecked = isSelected(item)

            cbCustomer.setOnClickListener {
                onToggle(item)
            }

            root.setOnClickListener {
                onToggle(item)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<CustomerItem>) {
        list = newList
        notifyDataSetChanged()
    }

    fun refresh() {
        notifyDataSetChanged()
    }
}