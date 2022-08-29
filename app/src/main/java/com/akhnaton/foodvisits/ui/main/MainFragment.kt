package com.akhnaton.foodvisits.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.chart.ChartIntent
import com.akhnaton.foodvisits.data.statusValue.chart.ChartStatus
import com.akhnaton.foodvisits.data.model.chart.ChartInfo
import com.akhnaton.foodvisits.databinding.FragmentMainBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private var mAdapter: ChartDataAdapter = ChartDataAdapter()

    private var currentPage = 0
    private var timer: Timer? = null
    private val DELAY_MS: Long = 500
    private val PERIOD_MS: Long = 3000


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)

        lifecycleScope.launch {
            viewModel.chartIntent.send(
                ChartIntent.Chart(
                    "1.0",
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }

        setupRecycler()
        fetchData()
        sliderImage()
        return binding.root
    }

    private fun setupRecycler() {
        binding.recyclerChartData.adapter = mAdapter
        binding.recyclerChartData.apply {
            layoutManager = LinearLayoutManager(activity)
            val decoration = DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
            addItemDecoration(decoration)
            binding.lifecycleOwner = activity
            binding.executePendingBindings()
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {

                    is ChartStatus.Idle -> Log.d(TAG, "makeLogin: ")
                    is ChartStatus.Loading -> Log.d(TAG, "makeLogin Loading: ")

                    is ChartStatus.ChartData -> {
                        if (it.data.status == 200) {
                            setChart(it.data.data.user_chart_info)
                            setAdapterData(it.data.data.user_chart_info)
                            Log.d(
                                TAG,
                                "makeLogin Login: ${it.data.data.user_chart_info[0].percentage}"
                            )
                        }else{
                            startActivity(Intent(requireContext(),LoginActivity::class.java))
                        }
                    }

                    is ChartStatus.Error -> {
                        Log.d(TAG, "makeLogin Error: ${it.error}")
                    }

                }
            }
        }
    }

    private fun setChart(mList: List<ChartInfo>) {
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        val entries: MutableList<PieEntry> = ArrayList()
        for (i in mList.indices) {
            if (mList[i].percentage != "0") {
                entries.add(
                    PieEntry(
                        (mList[i].percentage + "f").toFloat(),
                        mList[i].order_type
                    )
                )
            }
        }
        val set = PieDataSet(entries, "")
        val data = PieData(set)
        binding.piechart.setUsePercentValues(true)
        binding.piechart.getDescription().setEnabled(false)
        binding.piechart.animateY(1400, Easing.EaseInOutQuad)
        binding.piechart.setDragDecelerationFrictionCoef(0.95f)
        binding.piechart.setCenterText(generateCenterSpannableText())
        binding.piechart.setDrawHoleEnabled(true)
        binding.piechart.setTransparentCircleColor(Color.YELLOW)
        binding.piechart.setTransparentCircleAlpha(110)
        binding.piechart.setHoleRadius(58f)
        binding.piechart.setTransparentCircleRadius(61f)
        binding.piechart.setDrawCenterText(true)
        binding.piechart.setRotationAngle(0f)
        binding.piechart.setRotationEnabled(true)
        binding.piechart.setHighlightPerTapEnabled(true)
        set.colors = colors
        binding.piechart.setUsePercentValues(true)
        binding.piechart.setData(data)
        binding.piechart.invalidate() // refresh
    }

    private fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("Sales Achievement \nCustom Report")
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 4, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 13, s.length, 0)
        return s
    }

    private fun sliderImage() {

        val adapterView = SliderAdapter(requireContext())

        binding.slider.adapter = adapterView
        val handler = Handler(Looper.getMainLooper())

        val update = Runnable {
            if (currentPage == 5 - 1) {
                currentPage = 0
            }
            binding.slider.setCurrentItem(currentPage++, true)
        }

        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, DELAY_MS, PERIOD_MS)


    }

    private fun setAdapterData(data: List<ChartInfo>) {
        mAdapter.setChartDataList(data)
    }
}