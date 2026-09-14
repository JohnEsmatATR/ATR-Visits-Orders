package com.akhnaton.foodvisits.ui.home.visitPlan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.FragmentVisitPlanBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitPlanFragment : Fragment() {

    private var _binding: FragmentVisitPlanBinding? = null
    private val binding get() = _binding!!

    private val monthCalendarBase: Calendar = Calendar.getInstance()
    private val weekCalendar: Calendar = Calendar.getInstance() // يمثل الأسبوع المعروض حاليًا
    private var isWeeklyView = false

    // TODO: لما تيجي تربطها بداتا حقيقية، حط هنا الأيام اللي فيها زيارات
    // مثال: setOf("2026-09-14", "2026-09-18")
    private val daysWithVisits: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBackContainer.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.chipWeeklyView.setOnClickListener { toggleView() }
        binding.ivNextPeriod.setOnClickListener { shiftWeek(1) }
        binding.ivPrevPeriod.setOnClickListener { shiftWeek(-1) }

        renderCalendar()
    }

    private fun toggleView() {
        isWeeklyView = !isWeeklyView
        if (isWeeklyView) {
            // نبدأ دايمًا بالأسبوع الحالي (اللي فيه النهاردة)
            weekCalendar.time = Calendar.getInstance().time
        }
        renderCalendar()
    }

    private fun shiftWeek(direction: Int) {
        weekCalendar.add(Calendar.WEEK_OF_YEAR, direction)
        renderCalendar()
    }

    private fun renderCalendar() {
        updateChipLabel()
        updateArrowsVisibility()
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
        val calendarToShow = if (isWeeklyView) weekCalendar else monthCalendarBase
        binding.tvMonthYear.text = sdf.format(calendarToShow.time)
    }

    // ================== عرض الشهر كامل ==================
    private fun buildMonthGrid() {
        binding.gridCalendarDays.removeAllViews()
        binding.gridCalendarDays.rowCount = 6

        val monthCalendar = monthCalendarBase.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
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

        val today = Calendar.getInstance()
        val isToday = dayCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                dayCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        tvDay.isSelected = isToday

        val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCalendar.time)
        viewDot.visibility = if (daysWithVisits.contains(dayKey)) View.VISIBLE else View.INVISIBLE

        return dayView
    }

    private fun addGridCell(view: View, inflater: LayoutInflater = LayoutInflater.from(requireContext())) {
        val params = android.widget.GridLayout.LayoutParams()
        params.width = 0
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        binding.gridCalendarDays.addView(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}