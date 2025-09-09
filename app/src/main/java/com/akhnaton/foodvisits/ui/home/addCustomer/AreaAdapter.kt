import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.akhnaton.foodvisits.data.model.createNewCustomer.Area
import android.widget.Filter
import android.widget.TextView

class AreaAdapter(
    context: Context,
    private val allGovernorates: List<Area>
) : ArrayAdapter<Area>(context, android.R.layout.simple_dropdown_item_1line, ArrayList(allGovernorates)) {

    private var filteredGovernorates: List<Area> = allGovernorates

    override fun getItem(position: Int): Area? {
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
                        it.name_ar.lowercase().contains(filterPattern)
                    }
                }
                results.count = (results.values as List<Area>).size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredGovernorates = results.values as List<Area>
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as TextView).text = getItem(position)?.name_ar
        return view
    }
}
