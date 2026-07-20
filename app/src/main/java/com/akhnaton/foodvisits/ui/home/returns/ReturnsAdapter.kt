package com.akhnaton.foodvisits.ui.home.order.products

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.startReturnData.Product
import com.akhnaton.foodvisits.databinding.ItemProductBinding
import com.akhnaton.foodvisits.shared.DialogUtils

class ReturnsAdapter(
    private val list: MutableList<Product>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<ReturnsAdapter.ViewHolder>() {

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

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]
        with(holder.binding) {
            tvProductName.text = item.DESCRIPTION
//            holder.binding.tvQuota.text = if (item.MANDATORY_QOUTA == "1") {
//                holder.binding.tvQuota.setTextColor(holder.binding.root.resources.getColor(R.color.red))
//                "كوتة إجبارية : ${item.QUOTA_QTY}"
//            } else {
//                holder.binding.tvQuota.setTextColor(holder.binding.root.resources.getColor(R.color.green))
//                "كوتة : ${item.QUOTA_QTY}"
//            }
            tvItemCode.text =
                "كود: ${item.ITEM_CODE}"
            if (item.SAVED_ITEMS.isNotEmpty() && item.SAVED_ITEMS != null) {
                tvPrice.visibility = View.VISIBLE
                tvCustPrice.visibility = View.VISIBLE
                tvPrice.text =
                    "${holder.itemView.context.getString(R.string.price2)}: ${item.SAVED_ITEMS[0].UNIT_PRICE} ${
                        holder.itemView.context.getString(R.string.currency)
                    }"
                tvCustPrice.text =
                    "${holder.itemView.context.getString(R.string.cust_price)}: ${item.SAVED_ITEMS[0].CUSTOMER_PRICE} ${
                        holder.itemView.context.getString(R.string.currency)
                    }"
            } else {
                tvPrice.visibility = View.GONE
                tvCustPrice.visibility = View.GONE
            }
            tvSegment2.text =
                item.SEGMENT2
            if (item.TOTAL_QUANTITY >= 0) {
                tvQuantity.visibility = View.VISIBLE
                tvQuantity.text =
                    "${holder.itemView.context.getString(R.string.quantity)}: ${item.TOTAL_QUANTITY}"
            } else {
                tvQuantity.visibility = View.GONE
            }
//            ivDelete.visibility = View.GONE

            tvRequested.text =
                etQty.text.toString()

            tvMessage.text = item.MESSAGE

            if (item.MESSAGE != null) {
                if (item.MESSAGE.isNotEmpty())
                    tvMessage.visibility = View.VISIBLE
                else
                    tvMessage.visibility = View.GONE
            }

            val qty = item.selectedQty

            if (etQty.text.toString() != qty.toString()) {
                holder.ignoreWatcher = true
                etQty.setText(qty.toString())
                holder.ignoreWatcher = false
            }

            Log.d("WHATyouSay", "${item.CHECKED}")
            Log.d("WHATyouSay", "${item.selectedQty}")
            Log.d("WHATyouSay", "${item.TOTAL_QUANTITY}")

//            if (item.CHECKED == true) {
//                if (item.IS_BACK_ORDER == true) {
//                    if (item.selectedQty > item.TOTAL_QUANTITY) {
//                        tvBackOrder.visibility = View.VISIBLE
//                        val backOrder =
//                            item.selectedQty - item.TOTAL_QUANTITY
//                        tvBackOrder.text =
//                            "${holder.binding.root.context.getString(R.string.remaining)}: $backOrder"
//                        etQty.setText("${item.selectedQty}")
//                    }
//                } else if (item.IS_BACK_ORDER == false) {
//                    etQty.setText("${item.selectedQty}")
//                }
//            }

            updateSelectionUI(
                qty,
                item,
                holder.binding
            )

            btnPlus.setOnClickListener {
                item.selectedQty++

                holder.ignoreWatcher = true
                etQty.setText(item.selectedQty.toString())
                holder.ignoreWatcher = false

                updateSelectionUI(item.selectedQty, item, holder.binding)
                listener.onQuantityChanged(item)
            }

            btnMinus.setOnClickListener {
                if (item.selectedQty > 1) {
                    item.selectedQty--

                    holder.ignoreWatcher = true
                    etQty.setText(item.selectedQty.toString())
                    holder.ignoreWatcher = false

                    updateSelectionUI(item.selectedQty, item, holder.binding)
                    listener.onQuantityChanged(item)
                } else {
                    listener.onDeleteClicked(item)
                }
            }

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

                override fun afterTextChanged(s: Editable?) {
                    if (holder.ignoreWatcher) return

                    val text = s?.toString().orEmpty()

                    if (text.isBlank()) {
                        item.selectedQty = 0
                        updateSelectionUI(0, item, holder.binding)
                        listener.onQuantityChanged(item)
                        return
                    }

                    val qty = text.toIntOrNull() ?: return

                    item.selectedQty = qty
                    updateSelectionUI(qty, item, holder.binding)
                    listener.onQuantityChanged(item)
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
        val availableQty = item.TOTAL_QUANTITY ?: 0

        with(binding) {
            if (item.CHECKED) {
                if (item.IS_BACK_ORDER == true) {
                    tvBackOrder.visibility = View.VISIBLE
                    val backOrder =
                        maxOf(0, item.selectedQty - item.TOTAL_QUANTITY)
                    tvBackOrder.text =
                        "${binding.root.context.getString(R.string.remaining)}: $backOrder"
                } else {
                    tvBackOrder.visibility = View.GONE
                }

            }
            if (qty > 0) {

                tvZeroState.visibility = View.GONE

                tvRequested.visibility = View.VISIBLE
                tvTotalPrice.visibility = View.VISIBLE

                tvRequested.text =
                    "${binding.root.context.getString(R.string.requested)}: $qty"

//                val totalPrice =
//                    "${binding.root.context.getString(R.string.total)}: %.2f".format(qty * item.SAVED_ITEMS.UNIT_PRICE.toDouble())

                if (item.SAVED_ITEMS.isNotEmpty() && item.SAVED_ITEMS != null) {
                    tvTotalPrice.visibility = View.VISIBLE
                    val totalPrice =
                        "${binding.root.context.getString(R.string.total)}: ${item.SAVED_ITEMS[0].TOTAL_VALUE.toDouble()}"
                    tvTotalPrice.text =
                        "${totalPrice} ${binding.root.context.getString(R.string.currency)}"
                } else {
                    tvTotalPrice.visibility = View.GONE
                }

//                if (item.IS_BACK_ORDER == true && item.selectedQty > item.TOTAL_QUANTITY) {
//                    tvBackOrder.visibility = View.VISIBLE
//                    val backOrder =
//                        item.selectedQty - item.TOTAL_QUANTITY
//                    tvBackOrder.text =
//                        "${binding.root.context.getString(R.string.remaining)}: $backOrder"
//                    etQty.setText("$backOrder")
//                } else if (item.IS_BACK_ORDER == false && item.selectedQty > item.TOTAL_QUANTITY) {
//                    val backOrder =
//                        item.selectedQty - item.TOTAL_QUANTITY
//                    tvBackOrder.text =
//                        "${binding.root.context.getString(R.string.remaining)}: $backOrder"
//                }

//                if (item.IS_BACK_ORDER && item.selectedQty > item.TOTAL_QUANTITY) {
//                    tvBackOrder.visibility = View.VISIBLE
//                    val backOrder =
//                        item.selectedQty - item.TOTAL_QUANTITY
//
//                    tvBackOrder.text =
//                        "${binding.root.context.getString(R.string.remaining)}: $backOrder"
//                } else if (item.IS_BACK_ORDER && item.TOTAL_QUANTITY == 0) {
//                    tvBackOrder.visibility = View.VISIBLE
//                    tvBackOrder.text =
//                        "${binding.root.context.getString(R.string.remaining)}: ${item.selectedQty}"
//                } else if (!item.IS_BACK_ORDER) {
//                    tvBackOrder.visibility = View.GONE
//                }

                ivDelete.visibility = View.VISIBLE
                ivDelete.setOnClickListener {
                    listener.onDeleteClicked(item)
                }

            } else {
                tvZeroState.visibility = View.VISIBLE

                tvRequested.visibility = View.GONE
                tvTotalPrice.visibility = View.GONE
                tvBackOrder.visibility = View.GONE
                tvMessage.visibility = View.GONE

                tvZeroState.text =
                    "لم يتم تحديد الكمية"
            }
        }
    }

    interface OnItemActionListener {

        fun onItemClicked(
            item: Product
        )

        fun onQuantityChanged(
            item: Product,
        )

        fun onDeleteClicked(
            item: Product
        )
    }
}