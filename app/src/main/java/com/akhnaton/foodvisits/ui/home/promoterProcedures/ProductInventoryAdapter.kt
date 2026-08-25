package com.akhnaton.foodvisits.ui.home.inventory

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.promoterGetItemData.Data
import com.akhnaton.foodvisits.databinding.ItemProductInventoryBinding

class ProductInventoryAdapter(
    private val onSaveClick: (Data) -> Unit
) : RecyclerView.Adapter<ProductInventoryAdapter.ViewHolder>() {

    private val items = ArrayList<Data>()

    fun setData(newItems: List<Data>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<Data> {
        return items
    }

    inner class ViewHolder(
        private val binding: ItemProductInventoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Data) {
            binding.tvProductName.text = item.description
            binding.tvBrand.text = item.segment3
            binding.tvProductCode.text = "الكود: ${item.inventory_item_id}"
            binding.tvBarcode.text = item.item_code

//            binding.etQuantity.setText(item.quantity.toString())
//            binding.etPrice.setText(item.cust_price.toString())
//            binding.etReturned.setText(item.percentage_rate.toString())

            binding.etQuantity.afterTextChangedDelayed {
                if (it.isNotEmpty() || it.isNotBlank()) item.hasChanges = true
                else if (it.isEmpty() || it.isBlank()) item.hasChanges = false
            }

            binding.etPrice.afterTextChangedDelayed {
                if (it.isNotEmpty() || it.isNotBlank()) item.hasChanges = true
                else if (it.isEmpty() || it.isBlank()) item.hasChanges = false
            }

            binding.etReturned.afterTextChangedDelayed {
                if (it.isNotEmpty() || it.isNotBlank()) item.hasChanges = true
                else if (it.isEmpty() || it.isBlank()) item.hasChanges = false
            }

            binding.btnSaveChanges.setOnClickListener {

                val quantity =
                    binding.etQuantity.text
                        .toString()
                        .toIntOrNull() ?: 0

                val price =
                    binding.etPrice.text
                        .toString()
                        .toDoubleOrNull() ?: 0.0

                val returned =
                    binding.etReturned.text
                        .toString()
                        .toIntOrNull() ?: 0

                item.writtenQuantity = quantity.toString()
                item.writtenPrice = price.toString()
                item.writtenReturned = returned.toString()

                onSaveClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemProductInventoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams

        params.bottomMargin =
            if (position == items.lastIndex) {
                holder.itemView.context.resources
                    .getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp)
            } else {
                0
            }
        holder.bind(items[position])
    }

    private fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(1000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }
}