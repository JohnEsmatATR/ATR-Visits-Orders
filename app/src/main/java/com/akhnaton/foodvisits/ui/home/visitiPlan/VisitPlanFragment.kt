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
import androidx.core.widget.addTextChangedListener
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
import com.google.gson.Gson
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

    private var moveDialogCalendar: Calendar = Calendar.getInstance()
    private var moveDialogSelectedDate: Calendar? = null

    private var allVisits: List<VisitItem> = emptyList()
    private var isFirstLoad = true

    private var searchQuery: String = ""

    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false

    private var isSelectionMode = false
    private val selectedVisitIds: MutableSet<String> = mutableSetOf()

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

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            isFirstLoad = false
        } else {
            getData()
        }
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
        binding.fabAddVisit.setOnClickListener {
            findNavController().navigate(R.id.toAddVisitPlan)
        }
        binding.chipWeeklyView.setOnClickListener { toggleView() }
        binding.ivPrevPeriod.setOnClickListener { shiftWeek(-1) }
        binding.ivNextPeriod.setOnClickListener { shiftWeek(1) }

        binding.etSearch.addTextChangedListener { text ->
            searchQuery = text?.toString().orEmpty()
            filterVisitsForSelectedDate()
        }

        binding.tvSelectMode.setOnClickListener {
            isSelectionMode = true
            updateSelectionUI()
        }

        binding.tvCancelSelection.setOnClickListener {
            isSelectionMode = false
            selectedVisitIds.clear()
            updateSelectionUI()
        }

        binding.tvSelectAll.setOnClickListener {
            toggleSelectAllForSelectedDate()
        }

        binding.tvDeleteSelected.setOnClickListener {
            showBulkDeleteConfirmDialog()
        }
    }

    private fun setupRecycler() {
        adapter = VisitsAdapter(
            emptyList(),
            onItemClick = { item ->
                Log.d(TAG, "clicked: ${item.id}")
            },
            onSwapClick = { item ->
                showMoveVisitDialog(visitId = item.id)
            },
            onDeleteClick = { item ->
                showDeleteConfirmDialog(visitIds = listOf(item.id))
            },
            onSelectToggle = { item ->
                toggleVisitSelected(item.id)
            },
        )
        binding.rvVisits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@VisitPlanFragment.adapter
        }
    }

    private fun toggleVisitSelected(visitId: String) {
        if (selectedVisitIds.contains(visitId)) {
            selectedVisitIds.remove(visitId)
        } else {
            selectedVisitIds.add(visitId)
        }
        updateSelectionUI()
    }

    private fun toggleSelectAllForSelectedDate() {
        val visitsForSelectedDate = getVisitsForSelectedDate()
        val allSelected = visitsForSelectedDate.isNotEmpty() &&
                visitsForSelectedDate.all { selectedVisitIds.contains(it.id) }

        if (allSelected) {
            visitsForSelectedDate.forEach { selectedVisitIds.remove(it.id) }
        } else {
            visitsForSelectedDate.forEach { selectedVisitIds.add(it.id) }
        }
        updateSelectionUI()
    }

    private fun getVisitsForSelectedDate(): List<VisitItem> {
        val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
        return allVisits.filter { it.start.take(10) == selectedDateKey }
    }

    private fun updateSelectionUI() {
        binding.tvSelectMode.visibility = if (isSelectionMode) View.GONE else View.VISIBLE
        binding.llSelectionControls.visibility = if (isSelectionMode) View.VISIBLE else View.GONE

        val visitsForSelectedDate = getVisitsForSelectedDate()
        val allSelected = visitsForSelectedDate.isNotEmpty() &&
                visitsForSelectedDate.all { selectedVisitIds.contains(it.id) }
        binding.tvSelectAll.text = getString(
            if (allSelected) R.string.deselect_all_action else R.string.select_all_action
        )

        binding.tvDeleteSelected.visibility = if (isSelectionMode && selectedVisitIds.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvDeleteSelected.text = getString(R.string.delete_selected_format, selectedVisitIds.size)

        adapter.setSelectionMode(isSelectionMode)
        adapter.setSelectedIds(selectedVisitIds.toSet())
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

    private fun handleResponse(
        code: Int,
        message: String?,
        retry: () -> Unit,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "response code=$code message=$message retried=$hasRetriedAfterRefresh")
        when (code) {
            200 -> {
                hasRetriedAfterRefresh = false
                onSuccess()
            }

            401 -> {
                if (hasRetriedAfterRefresh) {
                    hasRetriedAfterRefresh = false
                    pendingRetry = null
                    showSessionExpired(message)
                } else {
                    pendingRetry = retry
                    sendRefreshToken()
                }
            }

            else -> {
                hasRetriedAfterRefresh = false
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = message.orEmpty(),
                    isSuccess = false,
                    showOkButton = true,
                )
            }
        }
    }

    private fun showSessionExpired(message: String?) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message.orEmpty(),
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(
                    Intent(requireContext(), LoginActivity2::class.java)
                )
                requireActivity().finishAffinity()
            }
        )
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
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getData() }
                            ) {
                                val visitData = Gson().fromJson(
                                    status.response.data,
                                    com.akhnaton.foodvisits.data.model.visitPlan.VisitData::class.java
                                )
                                allVisits = visitData.visits
                                filterVisitsForSelectedDate()
                                renderCalendar()
                            }
                        }

                        is VisitStatus.RefreshToken -> {
                            binding.progressLoading.visibility = View.GONE
                            Log.d(TAG, "refreshToken status=${status.data.status} message=${status.data.message}")
                            if (status.data.status == 200) {
                                val tokenData = Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                                hasRetriedAfterRefresh = true
                                val retry = pendingRetry
                                pendingRetry = null
                                retry?.invoke()
                            } else {
                                pendingRetry = null
                                hasRetriedAfterRefresh = false
                                showSessionExpired(status.data.message)
                            }
                        }

                        is VisitStatus.UpdateVisitDate -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getData() }
                            ) {
                                getData()
                            }
                        }

                        is VisitStatus.CopyPlan -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { showCopyPlanBottomSheet() }
                            ) {
                                val copyPlanData = Gson().fromJson(
                                    status.response.data,
                                    com.akhnaton.foodvisits.data.model.visitPlan.CopyPlanData::class.java
                                )
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = copyPlanData.message,
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = { getData() }
                                )
                            }
                        }

                        is VisitStatus.DeleteVisitPlan -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getData() }
                            ) {
                                isSelectionMode = false
                                selectedVisitIds.clear()
                                getData()
                            }
                        }

                        is VisitStatus.Error -> {
                            Log.d(TAG, "observeStatus: ${status.message}")
                            binding.progressLoading.visibility = View.GONE
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = status.message.orEmpty(),
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
        val query = searchQuery.normalizeArabic()

        val filteredList = allVisits.filter { visit ->
            val visitDateOnly = visit.start.take(10)
            val matchesDate = visitDateOnly == selectedDateKey
            val matchesSearch = query.isBlank() ||
                    visit.customer_name.normalizeArabic().contains(query) ||
                    visit.customer_code.normalizeArabic().contains(query) ||
                    visit.site_address.normalizeArabic().contains(query) ||
                    visit.sales_man.normalizeArabic().contains(query)
            matchesDate && matchesSearch
        }

        adapter.setActionsVisible(!isSelectedDatePast())
        adapter.updateList(filteredList)
        binding.tvVisitCount.text = getString(R.string.visits_count_format, filteredList.size)

        val isEmpty = filteredList.isEmpty()
        binding.rvVisits.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.llZeroState.visibility = if (isEmpty) View.VISIBLE else View.GONE

        updateSelectionUI()
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
            addGridCell(binding.gridCalendarDays, emptyView)
        }

        for (day in 1..daysInMonth) {
            val dayCalendar = monthCalendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            addGridCell(binding.gridCalendarDays, buildDayView(dayCalendar))
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
            addGridCell(binding.gridCalendarDays, buildDayView(dayCalendar))
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

    private fun addGridCell(grid: android.widget.GridLayout, view: View) {
        val params = android.widget.GridLayout.LayoutParams()
        params.width = 0
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        grid.addView(view)
    }

    private fun showMoveVisitDialog(visitId: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_move_visit, null)
        dialog.setContentView(view)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        moveDialogCalendar = Calendar.getInstance()
        moveDialogSelectedDate = null

        val tvMonthYear = view.findViewById<android.widget.TextView>(R.id.tv_move_month_year)
        val grid = view.findViewById<android.widget.GridLayout>(R.id.grid_move_calendar_days)
        val ivPrevMonth = view.findViewById<View>(R.id.iv_move_prev_month)
        val ivNextMonth = view.findViewById<View>(R.id.iv_move_next_month)
        val ivClose = view.findViewById<View>(R.id.iv_close_move)
        val btnCancel = view.findViewById<View>(R.id.btn_cancel_move)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm_move)

        ivPrevMonth.visibility = View.GONE
        ivNextMonth.visibility = View.GONE

        fun updateMoveMonthLabel() {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale("ar"))
            tvMonthYear.text = sdf.format(moveDialogCalendar.time)
        }

        fun buildMoveGrid() {
            grid.removeAllViews()
            val monthCalendar = moveDialogCalendar.clone() as Calendar
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val inflater = LayoutInflater.from(requireContext())

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            for (i in 0 until firstDayOfWeek) {
                val emptyView = inflater.inflate(R.layout.item_calendar_day, grid, false)
                emptyView.visibility = View.INVISIBLE
                addGridCell(grid, emptyView)
            }

            for (day in 1..daysInMonth) {
                val dayCalendar = monthCalendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_MONTH, day)
                dayCalendar.set(Calendar.HOUR_OF_DAY, 0)
                dayCalendar.set(Calendar.MINUTE, 0)
                dayCalendar.set(Calendar.SECOND, 0)
                dayCalendar.set(Calendar.MILLISECOND, 0)

                val dayView = inflater.inflate(R.layout.item_calendar_day, grid, false)
                val tvDay = dayView.findViewById<android.widget.TextView>(R.id.tv_day)
                val viewDot = dayView.findViewById<View>(R.id.view_dot)

                tvDay.text = day.toString()
                viewDot.visibility = View.INVISIBLE

                if (!dayCalendar.after(today)) {
                    dayView.alpha = 0.3f
                    dayView.isClickable = false
                    tvDay.isSelected = false
                    addGridCell(grid, dayView)
                    continue
                }

                dayView.alpha = 1.0f

                val selected = moveDialogSelectedDate
                val isSelected = selected != null &&
                        dayCalendar.get(Calendar.DAY_OF_MONTH) == selected.get(Calendar.DAY_OF_MONTH) &&
                        dayCalendar.get(Calendar.MONTH) == selected.get(Calendar.MONTH) &&
                        dayCalendar.get(Calendar.YEAR) == selected.get(Calendar.YEAR)

                tvDay.isSelected = isSelected

                dayView.setOnClickListener {
                    moveDialogSelectedDate = dayCalendar
                    buildMoveGrid()
                }

                addGridCell(grid, dayView)
            }
        }

        updateMoveMonthLabel()
        buildMoveGrid()

        ivClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val selected = moveDialogSelectedDate ?: return@setOnClickListener
            val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.time)
            viewModel.visitIntent.trySend(VisitIntent.UpdateVisitDate(visitId, newDate))
            dialog.dismiss()
        }

        dialog.show()
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

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val sdfMonth = SimpleDateFormat("MM-yyyy", Locale.US)
            val sourceDate = sdfMonth.format(monthCalendarBase.time)

            val targetCal = monthCalendarBase.clone() as Calendar
            targetCal.add(Calendar.MONTH, 1)
            val targetDate = sdfMonth.format(targetCal.time)

            viewModel.visitIntent.trySend(VisitIntent.CopyPlan(sourceDate, targetDate))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmDialog(visitIds: List<String>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_confirm_delete_visit, null)
        dialog.setContentView(view)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }

        val btnCancel = view.findViewById<View>(R.id.btn_cancel_delete)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm_delete)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            viewModel.visitIntent.trySend(
                VisitIntent.DeleteVisitPlan(visitIds.map { it.toIntOrNull() ?: 0 })
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showBulkDeleteConfirmDialog() {
        if (selectedVisitIds.isEmpty()) return
        showDeleteConfirmDialog(visitIds = selectedVisitIds.toList())
    }

    private fun isSelectedDatePast(): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val selected = (selectedCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return selected.before(today)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingRetry = null
        _binding = null
    }
    private fun String.normalizeArabic(): String {
        return this
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .trim()
            .lowercase()
    }
}