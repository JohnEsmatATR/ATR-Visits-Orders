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
import android.content.res.ColorStateList
import androidx.core.graphics.ColorUtils

class ChartInfoAdapter : RecyclerView.Adapter<ChartInfoAdapter.ViewHolder>() {

    private var list: List<ChartInfoItem> = emptyList()
    private var colors: List<Int> = emptyList()
    private val shortFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
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
        val color = colors[position]
        with(holder.binding) {
            model = item

            dot.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(color)
            }

            percentage.background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 100f
                setColor(ColorUtils.setAlphaComponent(color, 40))
            }
            percentage.setTextColor(color)
            percentage.text = "\u200E" + ((item.percentage * 100.0).roundToInt() / 100.0) + "%"

            sellValue.text = "\u200E"+ " ج.م" + numberFormat.format(item.sell_value)
            planValue.text = "\u200E"+ " ج.م" + numberFormat.format(item.plan_value)

            progress.progressTintList = ColorStateList.valueOf(color)
            progress.progress = item.percentage.toInt().coerceIn(0, 100)

            executePendingBindings()
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ChartInfoItem>) {
        list = newList
        val palette = ChartColors.list
        var chartIdx = 0
        var extraIdx = newList.count { it.percentage > 0 }
        colors = newList.map {
            if (it.percentage > 0) palette[chartIdx++ % palette.size]
            else palette[extraIdx++ % palette.size]
        }
        notifyDataSetChanged()
    }

    private fun formatShort(v: Double): String {
        val abs = kotlin.math.abs(v)
        val sign = if (v < 0) "-" else ""
        return when {
            abs >= 1_000_000 -> sign + shortFormat.format(abs / 1_000_000) + "M"
            abs >= 1_000 -> sign + shortFormat.format(abs / 1_000) + "K"
            else -> sign + numberFormat.format(abs)
        }
    }

}