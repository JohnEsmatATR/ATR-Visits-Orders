package com.akhnaton.foodvisits.ui.home.inventory

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.promoterGetItemData.Data
import com.akhnaton.foodvisits.databinding.ItemProductInventoryBinding

class ProductInventoryAdapter(
    private val onSaveClick: (Data) -> Unit
) : RecyclerView.Adapter<ProductInventoryAdapter.ViewHolder>() {

    private companion object {
        const val TAG = "ProductInventoryAdapter"
    }

    private val allItems =
        ArrayList<Data>()

    private val items =
        ArrayList<Data>()

    private val changedItemIds =
        mutableSetOf<String>()

    fun setData(
        newItems: List<Data>
    ) {

        allItems.clear()
        allItems.addAll(newItems)

        items.clear()
        items.addAll(newItems)

        changedItemIds.clear()

        items.forEach { item ->
            item.hasChanges = false
        }

        notifyDataSetChanged()
    }

    fun filter(
        query: String
    ) {
        val searchText = query.trim()
        items.clear()
        if (searchText.isEmpty()) {
            items.addAll(
                allItems
            )
        } else {
            items.addAll(
                allItems.filter { item ->
                    val description = item.description?.toString()?.trim().orEmpty()
                    val segment3 = item.segment3?.toString()?.trim().orEmpty()
                    val itemId = item.inventory_item_id?.toString()?.trim().orEmpty()
                    description.contains(searchText, ignoreCase = true) ||
                            segment3.contains(searchText, ignoreCase = true) ||
                            itemId.contains(searchText, ignoreCase = true)
                }
            )
        }
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<Data> {
        return items
    }

    fun getFilteredItemCount(): Int {
        return items.size
    }

    fun getChangedData(): List<Data> {

        return allItems.filter { item ->

            changedItemIds.contains(
                item.inventory_item_id.toString()
            )
        }
    }

    private fun markItemAsChanged(
        item: Data
    ) {
        val itemId = item.inventory_item_id.toString()
        val wasAlreadyChanged = changedItemIds.contains(itemId)
        changedItemIds.add(itemId)
        item.hasChanges = true
        Log.d(
            TAG,
            "ITEM MARKED AS CHANGED: itemId=$itemId, wasAlreadyChanged=$wasAlreadyChanged"
        )
        Log.d(
            TAG,
            "Current changedItemIds=$changedItemIds"
        )
    }

    fun clearChangedItem(
        item: Data
    ) {

        changedItemIds.remove(
            item.inventory_item_id.toString()
        )

        item.hasChanges = false
    }

    /**
     * Clear all changed items.
     */
    fun clearAllChangedItems() {

        changedItemIds.clear()

        items.forEach { item ->
            item.hasChanges = false
        }
    }

    /**
     * Number of items changed by the user.
     */
    fun getChangedItemCount(): Int {
        return changedItemIds.size
    }

    inner class ViewHolder(
        private val binding: ItemProductInventoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var quantityWatcher: TextWatcher? = null
        private var priceWatcher: TextWatcher? = null
        private var returnedWatcher: TextWatcher? = null

        private var isBinding = false

        fun bind(
            item: Data
        ) {

            Log.d(
                TAG,
                "bind() itemId=${item.inventory_item_id}"
            )

            isBinding = true

            /*
             * Remove old watchers first.
             */
            quantityWatcher?.let {
                binding.etQuantity.removeTextChangedListener(it)
            }

            priceWatcher?.let {
                binding.etPrice.removeTextChangedListener(it)
            }

            returnedWatcher?.let {
                binding.etReturned.removeTextChangedListener(it)
            }

            /*
             * Set normal item information.
             */
            binding.tvProductName.text =
                item.description

            binding.tvBrand.text =
                item.segment3

            binding.tvProductCode.text =
                "الكود: ${item.inventory_item_id}"

            binding.tvBarcode.text =
                item.item_code

            /*
             * Set the existing values.
             *
             * Watchers are currently removed,
             * so these setText() calls cannot mark
             * the item as changed.
             */
            binding.etQuantity.setText(
                item.writtenQuantity ?: ""
            )

            binding.etPrice.setText(
                item.writtenPrice ?: ""
            )

            binding.etReturned.setText(
                item.writtenReturned ?: ""
            )

            Log.d(
                TAG,
                "bind() values: itemId=${item.inventory_item_id}, " +
                        "quantity=${item.writtenQuantity}, " +
                        "price=${item.writtenPrice}, " +
                        "returned=${item.writtenReturned}"
            )

            /*
             * Create quantity watcher.
             */
            quantityWatcher =
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

                        if (isBinding) {
                            return
                        }

                        markItemAsChanged(
                            item
                        )
                    }
                }

            binding.etQuantity.addTextChangedListener(
                quantityWatcher
            )

            /*
             * Create price watcher.
             */
            priceWatcher =
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

                        if (isBinding) {
                            return
                        }

                        markItemAsChanged(
                            item
                        )
                    }
                }

            binding.etPrice.addTextChangedListener(
                priceWatcher
            )

            /*
             * Create returned quantity watcher.
             */
            returnedWatcher =
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

                        if (isBinding) {
                            return
                        }

                        markItemAsChanged(
                            item
                        )
                    }
                }

            binding.etReturned.addTextChangedListener(
                returnedWatcher
            )

            /*
             * Binding is finished.
             *
             * From this point onward, text changes
             * are considered user changes.
             */
            isBinding = false

            /*
             * Save button.
             *
             * IMPORTANT:
             * Do NOT call markItemAsChanged() here.
             */
            binding.btnSaveChanges.setOnClickListener {

                val quantity =
                    binding.etQuantity.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                val price =
                    binding.etPrice.text
                        .toString()
                        .toDoubleOrNull()
                        ?: 0.0

                val returned =
                    binding.etReturned.text
                        .toString()
                        .toIntOrNull()
                        ?: 0

                item.writtenQuantity =
                    quantity.toString()

                item.writtenPrice =
                    price.toString()

                item.writtenReturned =
                    returned.toString()

                onSaveClick(
                    item
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemProductInventoryBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return ViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val params =
            holder.itemView.layoutParams
                    as ViewGroup.MarginLayoutParams

        params.bottomMargin =
            if (position == items.lastIndex) {

                holder.itemView.context
                    .resources
                    .getDimensionPixelSize(
                        com.intuit.sdp.R.dimen._4sdp
                    )

            } else {
                0
            }

        holder.bind(
            items[position]
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }
}