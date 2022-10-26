package com.akhnaton.foodvisits.ui.home.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.chart.ChartInfo
import com.akhnaton.foodvisits.databinding.ListChartDataBinding
import kotlin.math.roundToInt

class ChartDataAdapter : RecyclerView.Adapter<ChartDataAdapter.ViewHolder>() {

    private var chartData = mutableListOf<ChartInfo>()

    fun setChartDataList(chartData: List<ChartInfo>) {
        this.chartData = chartData.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListChartDataBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chartData[position])
    }

    override fun getItemCount(): Int {
        return chartData.size
    }

    class ViewHolder(private val binding: ListChartDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChartInfo) {
            binding.model = data
            binding.percentage.text = ((data.percentage * 100.0).roundToInt() / 100.00).toString()
            binding.executePendingBindings()
        }
    }
}