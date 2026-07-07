package com.akhnaton.foodvisits.ui.home.order.products

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.databinding.ItemProductBinding

class ProductAdapter(
    private val list: MutableList<Product>,
    private val selections: MutableMap<String, Int>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var watcher: TextWatcher? = null
        var ignoreWatcher = false
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]


        with(holder.binding) {

            tvProductName.text = item.PRODUCT_NAME

            tvItemCode.text =
                "كود: ${item.ITEM_CODE}"

            tvPrice.text =
                "${holder.itemView.context.getString(R.string.price2)}: ${item.ITEM_PRICE} ${
                    holder.itemView.context.getString(R.string.currency)
                }"

            tvCustPrice.text =
                "${holder.itemView.context.getString(R.string.cust_price)}: ${item.CUST_PRICE} ${
                    holder.itemView.context.getString(R.string.currency)
                }"

            tvSegment2.text =
                item.SEGMENT2

            if (item.orderedQuantity > 0) {
                tvQuantity.visibility = View.VISIBLE
                tvQuantity.text =
                    "${holder.itemView.context.getString(R.string.quantity)}: ${item.orderedQuantity}"
            } else {
                tvQuantity.visibility = View.GONE
            }

            tvRequested.text =
                etQty.text.toString()

//            var backOrder = item.QUANTITY.toInt() - etQty.text.toString().toInt()
//            tvBackOrder.text =
//                "Back Order: ${backOrder}"

//            var totalPrice = item.ITEM_PRICE.toInt() * etQty.text.toString().toInt()
//            tvTotalPrice.text =
//                String.format("%.2f", totalPrice)

//            etQty.setText(item.selectedQty.toString())

            val qty =
                selections[item.ITEM_CODE] ?: 0

            if (etQty.text.toString() != qty.toString()) {
                holder.ignoreWatcher = true
                etQty.setText(qty.toString())
                holder.ignoreWatcher = false            }

            updateSelectionUI(
                qty,
                item,
                holder.binding
            )

            btnPlus.setOnClickListener {

                val qty =
                    (selections[item.ITEM_CODE] ?: 0) + 1

                selections[item.ITEM_CODE] = qty

                holder.ignoreWatcher = true
                etQty.setText(qty.toString())
                holder.ignoreWatcher = false

                updateSelectionUI(
                    qty,
                    item,
                    holder.binding
                )

                listener.onQuantityChanged(
                    item,
                    qty
                )
            }

            btnMinus.setOnClickListener {

                var qty =
                    (selections[item.ITEM_CODE] ?: 0) - 1

                if (qty < 0)
                    qty = 0

                if (qty == 0) {
                    selections.remove(item.ITEM_CODE)
                } else {
                    selections[item.ITEM_CODE] = qty
                }

                holder.ignoreWatcher = true
                etQty.setText(qty.toString())
                holder.ignoreWatcher = false

                updateSelectionUI(
                    qty,
                    item,
                    holder.binding
                )

                listener.onQuantityChanged(
                    item,
                    qty
                )
            }

//            etQty.addTextChangedListener(
//                object : TextWatcher {
//
//                    override fun beforeTextChanged(
//                        s: CharSequence?,
//                        start: Int,
//                        count: Int,
//                        after: Int
//                    ) {
//                    }
//
//                    override fun onTextChanged(
//                        s: CharSequence?,
//                        start: Int,
//                        before: Int,
//                        count: Int
//                    ) {
//                    }
//
//                    override fun afterTextChanged(
//                        s: Editable?
//                    ) {
//
//                        val qty =
//                            s.toString()
//                                .toIntOrNull() ?: 0
//
//                        if (qty == 0) {
//                            selections.remove(item.ITEM_CODE)
//                        } else {
//                            selections[item.ITEM_CODE] = qty
//                        }
//
//                        updateSelectionUI(
//                            qty,
//                            item,
//                            holder.binding
//                        )
//
//                        listener.onQuantityChanged(
//                            item,
//                            qty
//                        )
//                    }
//                }
//            )

            holder.watcher?.let {
                etQty.removeTextChangedListener(it)
            }

            holder.watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    if (holder.ignoreWatcher)
                        return
                }


            }

            etQty.addTextChangedListener(holder.watcher)

            root.setOnClickListener {
                listener.onItemClicked(item)
            }
        }
    }

    private fun updateSelectionUI(
        qty: Int,
        item: Product,
        binding: ItemProductBinding
    ) {
        val availableQty = item.QUANTITY.toIntOrNull() ?: 0

        with(binding) {

            if (qty > 0) {

                tvZeroState.visibility = View.GONE

                tvRequested.visibility = View.VISIBLE
                tvTotalPrice.visibility = View.VISIBLE

//                if (qty > availableQty) tvBackOrder.visibility = View.VISIBLE
//                else tvBackOrder.visibility = View.GONE

                tvRequested.text =
                    "${binding.root.context.getString(R.string.requested)}: $qty"

                val totalPrice =
                    "${binding.root.context.getString(R.string.total)}: %.2f".format(qty * item.ITEM_PRICE.toDouble())

                tvTotalPrice.text =
                    "${totalPrice} ${binding.root.context.getString(R.string.currency)}"

                val backOrder =
                    qty - availableQty

                tvBackOrder.text =
                    "${binding.root.context.getString(R.string.remaining)}: $backOrder"

            } else {

                tvZeroState.visibility = View.VISIBLE

                tvRequested.visibility = View.GONE
                tvTotalPrice.visibility = View.GONE

//                if (qty > availableQty) tvBackOrder.visibility = View.VISIBLE
//                else tvBackOrder.visibility = View.GONE

                tvZeroState.text =
                    "${binding.root.context.getString(R.string.no_product_choosen)}"
            }
        }
    }

    interface OnItemActionListener {

        fun onItemClicked(
            item: Product
        )

        fun onQuantityChanged(
            item: Product,
            qty: Int
        )
    }
}