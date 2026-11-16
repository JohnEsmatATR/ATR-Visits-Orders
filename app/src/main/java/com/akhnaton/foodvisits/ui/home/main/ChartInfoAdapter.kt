package com.akhnaton.foodvisits.ui.home.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.chartInfo.ChartInfoItem
import com.akhnaton.foodvisits.databinding.ListChartDataBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

class ChartInfoAdapter : RecyclerView.Adapter<ChartInfoAdapter.ViewHolder>() {

    private var list: List<ChartInfoItem> = emptyList()
    private val numberFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

    class ViewHolder(val binding: ListChartDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListChartDataBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            model = item
            percentage.text = ((item.percentage * 100.0).roundToInt() / 100.0).toString() + "%"
            sellValue.text = "المبيعات: " + numberFormat.format(item.sell_value) + " ج.م"
            planValue.text = "الخطة: " + numberFormat.format(item.plan_value) + " ج.م"
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ChartInfoItem>) {
        list = newList
        notifyDataSetChanged()
    }

}