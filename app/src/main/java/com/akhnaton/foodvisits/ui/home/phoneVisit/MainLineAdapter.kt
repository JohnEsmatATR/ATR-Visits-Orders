package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine

class MainLineAdapter(
    context: Context,
    private var items: List<MainCustomerLine>
) : ArrayAdapter<MainCustomerLine>(context, android.R.layout.simple_dropdown_item_1line, items), Filterable {

    private var filteredItems: List<MainCustomerLine> = items

    override fun getCount(): Int = filteredItems.size
    override fun getItem(position: Int): MainCustomerLine? = filteredItems.getOrNull(position)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults().apply {
                    values = if (constraint.isNullOrEmpty()) {
                        items
                    } else {
                        items.filter {
                            it.customer_name.contains(constraint, ignoreCase = true)
                        }
                    }
                    count = (values as List<*>).size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredItems = results?.values as? List<MainCustomerLine> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView

        view.text = getItem(position)?.customer_name
        return view
    }

    fun updateList(newItems: List<MainCustomerLine>) {
        items = newItems
        filteredItems = newItems
        notifyDataSetChanged()
    }
}