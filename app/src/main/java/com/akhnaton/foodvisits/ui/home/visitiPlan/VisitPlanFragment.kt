package com.akhnaton.foodvisits.ui.home.visitPlan

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visitPlan.VisitItem
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitPlanBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitPlanFragment : Fragment() {

    companion object {
        private const val TAG = "VisitPlanFragment"
    }

    private var _binding: FragmentVisitPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitPlanViewModel by viewModels()
    private lateinit var adapter: VisitsAdapter

    private val monthCalendarBase: Calendar = Calendar.getInstance()
    private val weekCalendar: Calendar = Calendar.getInstance()
    private var isWeeklyView = false
    private var selectedCalendar: Calendar = Calendar.getInstance()

    private var allVisits: List<VisitItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupListeners()
        observeStatus()
        getData()

        renderCalendar()
        selectDay(selectedCalendar)
    }

    private fun getData() {
        viewModel.visitIntent.trySend(VisitIntent.GetMonthlyVisits)
    }

    private fun setupListeners() {
        binding.fabDuplicate.setOnClickListener {
            showCopyPlanBottomSheet()
        }

        binding.btnBackContainer.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chipWeeklyView.setOnClickListener { toggleView() }
        binding.ivPrevPeriod.setOnClickListener { shiftWeek(-1) }
        binding.ivNextPeriod.setOnClickListener { shiftWeek(1) }
    }

    private fun setupRecycler() {
        adapter = VisitsAdapter(emptyList()) { item ->
            Log.d(TAG, "clicked: ${item.id}")
        }
        binding.rvVisits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@VisitPlanFragment.adapter
        }
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is VisitStatus.Loading -> {
                            binding.progressLoading.visibility = View.VISIBLE
                        }

                        is VisitStatus.GetMonthlyVisits -> {
                            binding.progressLoading.visibility = View.GONE
                            if (status.response.status == 401) {
                                sendRefreshToken()
                            } else {
                                allVisits = status.response.data.visits
                                filterVisitsForSelectedDate()
                                renderCalendar()
                            }
                        }

                        is VisitStatus.RefreshToken -> {
                            if (status.data.status == 200) {
                                val tokenData = com.google.gson.Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                                getData()
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = status.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(
                                            Intent(requireContext(), LoginActivity2::class.java)
                                        )
                                        requireActivity().finishAffinity()
                                    })
                            }
                        }

                        is VisitStatus.Error -> {
                            Log.d(TAG, "fetchData: ${status.message}")
                            binding.progressLoading.visibility = View.GONE
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = "خطأ",
                                isSuccess = false,
                                showOkButton = true,
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun filterVisitsForSelectedDate() {
        val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)

        val filteredList = allVisits.filter { visit ->
            val visitDateOnly = visit.start.take(10)
            visitDateOnly == selectedDateKey
        }

        adapter.updateList(filteredList)
        binding.tvVisitCount.text = getString(R.string.visits_count_format, filteredList.size)
    }

    private fun selectDay(calendar: Calendar) {
        selectedCalendar = calendar.clone() as Calendar
        filterVisitsForSelectedDate()

        val displayFormat = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
        binding.tvVisitDate.text = displayFormat.format(selectedCalendar.time)
    }

    private fun toggleView() {
        isWeeklyView = !isWeeklyView
        if (isWeeklyView) {
            weekCalendar.time = selectedCalendar.time
        }

        val transition = AutoTransition().apply {
            duration = 250
        }
        TransitionManager.beginDelayedTransition(binding.gridCalendarDays, transition)
        renderCalendar()
    }

    private fun shiftWeek(direction: Int) {
        val isRtl = ViewCompat.getLayoutDirection(binding.root) == ViewCompat.LAYOUT_DIRECTION_RTL
        val actualDirection = if (isRtl) -direction else direction

        val targetWeek = weekCalendar.clone() as Calendar
        targetWeek.add(Calendar.WEEK_OF_YEAR, actualDirection)

        if (!isWeekInCurrentMonth(targetWeek)) {
            return
        }

        val translationAmount = if (actualDirection > 0) -100f else 100f

        binding.gridCalendarDays.animate()
            .translationX(translationAmount)
            .alpha(0f)
            .setDuration(120)
            .withEndAction {
                weekCalendar.add(Calendar.WEEK_OF_YEAR, actualDirection)
                renderCalendar()

                binding.gridCalendarDays.translationX = -translationAmount
                binding.gridCalendarDays.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    private fun isWeekInCurrentMonth(calendar: Calendar): Boolean {
        val temp = calendar.clone() as Calendar
        temp.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val currentMonth = monthCalendarBase.get(Calendar.MONTH)
        val currentYear = monthCalendarBase.get(Calendar.YEAR)

        for (i in 0 until 7) {
            if (temp.get(Calendar.MONTH) == currentMonth && temp.get(Calendar.YEAR) == currentYear) {
                return true
            }
            temp.add(Calendar.DAY_OF_MONTH, 1)
        }
        return false
    }

    private fun updateArrowsEnabledState() {
        if (!isWeeklyView) return

        val isRtl = ViewCompat.getLayoutDirection(binding.root) == ViewCompat.LAYOUT_DIRECTION_RTL

        val prevWeek = weekCalendar.clone() as Calendar
        prevWeek.add(Calendar.WEEK_OF_YEAR, if (isRtl) 1 else -1)
        val canGoPrev = isWeekInCurrentMonth(prevWeek)

        val nextWeek = weekCalendar.clone() as Calendar
        nextWeek.add(Calendar.WEEK_OF_YEAR, if (isRtl) -1 else 1)
        val canGoNext = isWeekInCurrentMonth(nextWeek)

        binding.ivPrevPeriod.isEnabled = canGoPrev
        binding.ivPrevPeriod.alpha = if (canGoPrev) 1.0f else 0.3f

        binding.ivNextPeriod.isEnabled = canGoNext
        binding.ivNextPeriod.alpha = if (canGoNext) 1.0f else 0.3f
    }

    private fun renderCalendar() {
        updateChipLabel()
        updateArrowsVisibility()
        updateArrowsEnabledState()
        updateMonthYearLabel()

        if (isWeeklyView) {
            buildWeekRow()
        } else {
            buildMonthGrid()
        }
    }

    private fun updateChipLabel() {
        val labelRes = if (isWeeklyView) R.string.monthly_view else R.string.weekly_view
        binding.tvChipLabel.text = getString(labelRes)
    }

    private fun updateArrowsVisibility() {
        val visibility = if (isWeeklyView) View.VISIBLE else View.GONE
        binding.ivNextPeriod.visibility = visibility
        binding.ivPrevPeriod.visibility = visibility
    }

    private fun updateMonthYearLabel() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("ar"))
        binding.tvMonthYear.text = sdf.format(monthCalendarBase.time)
    }

    private fun buildMonthGrid() {
        binding.gridCalendarDays.removeAllViews()
        binding.gridCalendarDays.rowCount = 6

        val monthCalendar = monthCalendarBase.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val inflater = LayoutInflater.from(requireContext())

        for (i in 0 until firstDayOfWeek) {
            val emptyView = inflater.inflate(R.layout.item_calendar_day, binding.gridCalendarDays, false)
            emptyView.visibility = View.INVISIBLE
            addGridCell(emptyView)
        }

        for (day in 1..daysInMonth) {
            val dayCalendar = monthCalendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            addGridCell(buildDayView(dayCalendar))
        }
    }

    private fun buildWeekRow() {
        binding.gridCalendarDays.removeAllViews()
        binding.gridCalendarDays.rowCount = 1

        val startOfWeek = weekCalendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0 until 7) {
            val dayCalendar = startOfWeek.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_MONTH, i)
            addGridCell(buildDayView(dayCalendar))
        }
    }

    private fun buildDayView(dayCalendar: Calendar): View {
        val inflater = LayoutInflater.from(requireContext())
        val dayView = inflater.inflate(R.layout.item_calendar_day, binding.gridCalendarDays, false)

        val tvDay = dayView.findViewById<android.widget.TextView>(R.id.tv_day)
        val viewDot = dayView.findViewById<View>(R.id.view_dot)

        val day = dayCalendar.get(Calendar.DAY_OF_MONTH)
        tvDay.text = day.toString()

        val currentMonth = monthCalendarBase.get(Calendar.MONTH)
        val isDayInCurrentMonth = dayCalendar.get(Calendar.MONTH) == currentMonth

        if (!isDayInCurrentMonth) {
            dayView.alpha = 0.2f
            tvDay.isSelected = false
            viewDot.visibility = View.INVISIBLE
            dayView.isClickable = false
            return dayView
        }

        dayView.alpha = 1.0f

        val isSelected = dayCalendar.get(Calendar.DAY_OF_MONTH) == selectedCalendar.get(Calendar.DAY_OF_MONTH) &&
                dayCalendar.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
                dayCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)

        tvDay.isSelected = isSelected

        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCalendar.time)

        val hasVisits = allVisits.any { visit ->
            visit.start.take(10) == dayKey
        }
        viewDot.visibility = if (hasVisits) View.VISIBLE else View.INVISIBLE

        dayView.setOnClickListener {
            selectDay(dayCalendar)
            renderCalendar()
        }

        return dayView
    }
    private fun addGridCell(view: View) {
        val params = android.widget.GridLayout.LayoutParams()
        params.width = 0
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        binding.gridCalendarDays.addView(view)
    }

    private fun showCopyPlanBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_copy_plan, null)
        dialog.setContentView(view)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        val btnCancel = view.findViewById<View>(R.id.btn_cancel_copy)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm_copy)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun sendRefreshToken() {
        lifecycleScope.launch {
            viewModel.visitIntent.send(
                VisitIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}