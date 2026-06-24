package com.akhnaton.foodvisits.ui.home.order.products

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.databinding.ItemProductBinding

class ProductAdapter(
    private val list: MutableList<Product>,
    private val selections: MutableMap<String, Int>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root)

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

            tvDescription.text = item.DESCRIPTION

            tvItemCode.text =
                "كود: ${item.ITEM_CODE}"

            tvPrice.text =
                "السعر: ${item.ITEM_PRICE}"

            tvCustPrice.text =
                "جمهور: ${item.CUST_PRICE}"

            tvSegment2.text =
                item.SEGMENT2

            tvQuantity.text =
                "الكمية: ${item.QUANTITY}"

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

            btnPlus.setOnClickListener {
                val qty =
                    (selections[item.ITEM_CODE] ?: 0) + 1

                selections[item.ITEM_CODE] = qty

                listener.onQuantityChanged(
                    item,
                    qty
                )

                notifyItemChanged(position)
                etQty.setText(
                    qty.toString()
                )
                listener.onQuantityChanged(item, etQty.text.toString().toInt())
            }

            btnMinus.setOnClickListener {
                val qty =
                    (selections[item.ITEM_CODE] ?: 0) - 1

                if (qty <= 0) {

                    selections.remove(item.ITEM_CODE)

                    listener.onQuantityChanged(
                        item,
                        0
                    )

                } else {

                    selections[item.ITEM_CODE] = qty

                    listener.onQuantityChanged(
                        item,
                        qty
                    )
                }

                notifyItemChanged(position)
            }

            etQty.addTextChangedListener(
                object : TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(
                        s: Editable?
                    ) {

                        val qty =
                            s.toString()
                                .toIntOrNull() ?: 0

//                        item.selectedQty = qty

                        listener.onQuantityChanged(item, etQty.text.toString().toInt())
                    }
                }
            )

            root.setOnClickListener {
                listener.onItemClicked(item)
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