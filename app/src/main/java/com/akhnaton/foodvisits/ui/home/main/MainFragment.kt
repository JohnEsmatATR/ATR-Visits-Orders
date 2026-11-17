package com.akhnaton.foodvisits.ui.home.main

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
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.chartInfo.ChartInfoData
import com.akhnaton.foodvisits.data.model.chartInfo.ChartInfoItem
import com.akhnaton.foodvisits.data.statusValue.chartInfo.ChartInfoIntent
import com.akhnaton.foodvisits.data.statusValue.chartInfo.ChartInfoStatus
import com.akhnaton.foodvisits.shared.DialogUtils
import com.google.gson.Gson
import com.akhnaton.foodvisits.databinding.FragmentMainBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment() {

    companion object {
        private const val TAG = "MainFragment"
    }

   // private val versionName = BuildConfig.VERSION_NAME
   private val viewModel: ChartInfoViewModel by viewModels()
    private var hasRetriedAfterRefresh = false
    private lateinit var binding: FragmentMainBinding
    private var mAdapter: ChartInfoAdapter = ChartInfoAdapter()

    private var currentPage = 0
    private var timer: Timer? = null
    private val DELAY_MS: Long = 500
    private val PERIOD_MS: Long = 3000

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)
        binding.piechart.setNoDataText("")
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        binding.tvUsername.setText(SharedPreferencesHelper.getInstance().getUsername())
        showChartLoading(true)
        getData()

        setupRecycler()
        fetchData()
        sliderImage()
        handleTopBottomKeyboard()

        return binding.root
    }

    private fun setupRecycler() {
        binding.recyclerChartData.adapter = mAdapter
        binding.recyclerChartData.apply {
            layoutManager = LinearLayoutManager(activity)
            binding.lifecycleOwner = activity
            binding.executePendingBindings()
        }
    }
    private fun showChartLoading(show: Boolean) {
        binding.chartLoading.visibility = if (show) View.VISIBLE else View.GONE
        binding.piechart.alpha = if (show) 0.3f else 1f
        if (show) {
            binding.layoutZeroState.visibility = View.GONE
        }
    }
    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is ChartInfoStatus.Loading -> Log.d(TAG, "fetchData: Loading")

                    is ChartInfoStatus.GetChartInfo -> {
                        when (it.response.status) {
                            200 -> {
                                showChartLoading(false)
                                hasRetriedAfterRefresh = false
                                val data = Gson().fromJson(it.response.data, ChartInfoData::class.java)
                                val list = data?.user_chart_info.orEmpty()
                                Log.d(TAG, "chart list size=${list.size} -> $list")
                                if (list.isEmpty()) {
                                    binding.chartCard.visibility = View.VISIBLE
                                    binding.piechart.visibility = View.GONE
                                    binding.layoutZeroState.visibility = View.VISIBLE
                                    binding.tvOrderTypeTitle.visibility = View.GONE
                                    binding.details.visibility = View.GONE
                                } else {
                                    binding.chartCard.visibility = View.VISIBLE
                                    binding.piechart.visibility = View.VISIBLE
                                    binding.layoutZeroState.visibility = View.GONE
                                    binding.tvOrderTypeTitle.visibility = View.VISIBLE
                                    binding.details.visibility = View.VISIBLE
                                    setChart(list)
                                    setAdapterData(list)
                                }
//                                setChart(list)
//                                 setAdapterData(list)  // بعد ما نظبط الـ adapter
                            }

                            401 -> {
                                if (hasRetriedAfterRefresh) {
                                    showChartLoading(false)
                                    hasRetriedAfterRefresh = false
                                    showSessionExpired(it.response.message)
                                } else {
                                    sendRefreshToken()
                                }
                            }

                            else -> {
                                showChartLoading(false)
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.response.message,
                                    isSuccess = false,
                                    showOkButton = true
                                )
                            }
                        }
                    }

                    is ChartInfoStatus.RefreshToken -> {
                        if (it.data.status == 200) {
                            val tokenData = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                            )
                            SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                            hasRetriedAfterRefresh = true
                            getData()
                        } else {
                            showChartLoading(false)
                            showSessionExpired(it.data.message)
                        }
                    }

                    is ChartInfoStatus.Error -> {
                        showChartLoading(false)
                        Log.d(TAG, "fetchData Error: ${it.message}")
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setChart(mList: List<ChartInfoItem>) {
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        val totalSell = mList.sumOf { it.sell_value.toDouble() }
        val entries: MutableList<PieEntry> = ArrayList()
        if (totalSell > 0) {
            for (item in mList) {
                val percentage = item.sell_value.toDouble() / totalSell * 100.0
                if (percentage > 0) {
                    entries.add(PieEntry(percentage.toFloat(), item.order_type))
                }
            }
        }

        val set = PieDataSet(entries, "")
        val data = PieData(set)
        data.setValueFormatter(PercentFormatter(binding.piechart))
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(14f)
        data.setValueTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        binding.piechart.setUsePercentValues(true)
        binding.piechart.description.isEnabled = false
        binding.piechart.animateY(1400, Easing.EaseInOutQuad)
        binding.piechart.dragDecelerationFrictionCoef = 0.95f
        binding.piechart.centerText = generateCenterSpannableText()
        binding.piechart.isDrawHoleEnabled = true
        binding.piechart.setTransparentCircleColor(Color.YELLOW)
        binding.piechart.setTransparentCircleAlpha(110)
        binding.piechart.holeRadius = 58f
        binding.piechart.transparentCircleRadius = 61f
        binding.piechart.setDrawCenterText(true)
        binding.piechart.setDrawEntryLabels(false)
        binding.piechart.legend.isWordWrapEnabled = true
        binding.piechart.rotationAngle = 0f
        binding.piechart.isRotationEnabled = true
        binding.piechart.isHighlightPerTapEnabled = true
        set.colors = colors
        binding.piechart.setUsePercentValues(true)
        binding.piechart.data = data
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

    private fun setAdapterData(data: List<ChartInfoItem>) {
        mAdapter.updateList(data)
    }
    private fun getData() {
        viewModel.chartInfoIntent.trySend(ChartInfoIntent.GetChartInfo)
    }

    private fun sendRefreshToken() {
        viewModel.chartInfoIntent.trySend(
            ChartInfoIntent.RefreshToken(
                SharedPreferencesHelper.getInstance().getEmployeeId(),
                SharedPreferencesHelper.getInstance().getUserToken()
            )
        )
    }
    private fun handleTopBottomKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(
                    imeInsets.bottom,
                    systemBars.bottom
                )
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }
    private fun showSessionExpired(message: String?) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message.orEmpty(),
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(Intent(requireContext(), LoginActivity2::class.java))
                requireActivity().finishAffinity()
            }
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        (activity as? MainActivity)?.let {
//            it.findViewById<BottomNavigationView>(R.id.nav_view2).visibility = View.VISIBLE
//        }
    }
}