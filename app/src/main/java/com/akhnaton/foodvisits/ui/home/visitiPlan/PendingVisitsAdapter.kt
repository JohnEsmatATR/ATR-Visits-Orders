package com.akhnaton.foodvisits.ui.home.visitiPlan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.pendingVisits.PendingVisitItem
import com.akhnaton.foodvisits.databinding.ItemPendingVisitCardBinding

class PendingVisitsAdapter(
    private var items: List<PendingVisitItem>,
    private val onApproveClick: (PendingVisitItem) -> Unit,
    private val onRejectClick: (PendingVisitItem) -> Unit,
    private val onSelectToggle: (PendingVisitItem) -> Unit,
) : RecyclerView.Adapter<PendingVisitsAdapter.ViewHolder>() {

    private var selectedIds: Set<String> = emptySet()

    class ViewHolder(val binding: ItemPendingVisitCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingVisitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvPharmacyName.text = item.CUSTOMER_NAME
            tvVisitNumber.text = root.context.getString(R.string.visit_number_format, item.ID)
            tvDelegate.text = root.context.getString(R.string.delegate_format, item.LAST_NAME)
            tvProposedDate.text = root.context.getString(R.string.proposed_date_format, item.DATE_OF_VISIT)

            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = selectedIds.contains(item.ID)
            cbSelect.setOnCheckedChangeListener { _, _ -> onSelectToggle(item) }

            btnApprove.setOnClickListener { onApproveClick(item) }
            btnReject.setOnClickListener { onRejectClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<PendingVisitItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun appendList(newItems: List<PendingVisitItem>) {
        val startPosition = items.size
        items = items + newItems
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    fun setSelectedIds(ids: Set<String>) {
        selectedIds = ids
        notifyDataSetChanged()
    }

    fun getAllIds(): List<String> = items.map { it.ID }
}