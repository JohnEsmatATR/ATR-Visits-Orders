package com.akhnaton.foodvisits.ui.home.addCustomer

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.akhnaton.foodvisits.data.model.VisitsData
import com.akhnaton.foodvisits.data.model.createNewCustomer.Governorate


class OrderTypeAdapter(
    context: Context,
    private val allGovernorates: List<VisitsData>
) : ArrayAdapter<VisitsData>(context, R.layout.simple_dropdown_item_1line, ArrayList(allGovernorates)) {

    private var filteredGovernorates: List<VisitsData> = allGovernorates

    override fun getItem(position: Int): VisitsData? {
        return filteredGovernorates[position]
    }

    override fun getCount(): Int = filteredGovernorates.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = if (constraint.isNullOrEmpty()) {
                    allGovernorates
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    allGovernorates.filter {
                        it.user_order_type.contains(filterPattern)
                    }
                }
                results.count = (results.values as List<Governorate>).size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredGovernorates = results.values as List<VisitsData>
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as TextView).text = getItem(position)?.user_order_type as CharSequence?
        return view
    }
}
