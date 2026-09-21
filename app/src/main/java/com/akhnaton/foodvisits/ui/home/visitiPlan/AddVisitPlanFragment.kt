package com.akhnaton.foodvisits.ui.home.visitPlan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visitPlan.CustomerItem
import com.akhnaton.foodvisits.data.model.visitPlan.LineItem
import com.akhnaton.foodvisits.data.model.visitPlan.SaveCustomerRequest
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitStatus
import com.akhnaton.foodvisits.databinding.FragmentAddVisitPlanBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddVisitPlanFragment : Fragment() {

    companion object {
        private const val TAG = "AddVisitPlanFragment"
    }

    private var _binding: FragmentAddVisitPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddVisitPlanViewModel by viewModels()

    private var salesTypes: List<String> = emptyList()
    private var selectedSaleType: String? = null

    private var lines: List<LineItem> = emptyList()
    private var selectedLine: LineItem? = null
    private lateinit var lineAdapter: LineAdapter

    private var customersList: List<CustomerItem> = emptyList()
    private val selectedCustomerIds: MutableSet<String> = mutableSetOf()
    private lateinit var customersAdapter: CustomersAdapter

    private val baseMonthCalendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    private var visibleMonthOffset = 0
    private val selectedVisitDates: MutableSet<String> = mutableSetOf()
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVisitPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupExpandableSections()
        setupRoutesRecyclerView()
        setupVisitCalendar()
        observeStatus()
        getSalesTypes()
        setupCustomersRecyclerView()
        updateSaveButtonState()

        binding.cardRoute.visibility = View.GONE
        binding.cardCustomers.visibility = View.GONE
        binding.cardRoute.visibility = View.GONE
        binding.cardCustomers.visibility = View.GONE

        binding.contentSaleType.visibility = View.VISIBLE
        binding.ivChevronSaleType.rotation = 180f
    }

    private fun setupVisitCalendar() {
        binding.ivVisitPrevMonth.setOnClickListener {
            if (visibleMonthOffset > 0) {
                visibleMonthOffset = 0
                refreshVisitCalendar()
            }
        }
        binding.ivVisitNextMonth.setOnClickListener {
            if (visibleMonthOffset < 1) {
                visibleMonthOffset = 1
                refreshVisitCalendar()
            }
        }
        refreshVisitCalendar()
    }

    private fun visibleMonth(): Calendar {
        val month = baseMonthCalendar.clone() as Calendar
        month.add(Calendar.MONTH, visibleMonthOffset)
        return month
    }

    private fun refreshVisitCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("ar"))
        binding.tvVisitMonthYear.text = sdf.format(visibleMonth().time)
        val activeColor = Color.parseColor("#FF8A00")
        val disabledColor = Color.parseColor("#C4C4C4")
        binding.ivVisitPrevMonth.imageTintList =
            ColorStateList.valueOf(if (visibleMonthOffset > 0) activeColor else disabledColor)
        binding.ivVisitNextMonth.imageTintList =
            ColorStateList.valueOf(if (visibleMonthOffset < 1) activeColor else disabledColor)
        buildVisitCalendarGrid()
    }

    private fun buildVisitCalendarGrid() {
        val grid = binding.gridVisitCalendarDays
        grid.removeAllViews()
        grid.rowCount = 6

        val monthCalendar = visibleMonth()
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
            addVisitGridCell(grid, emptyView)
        }

        for (day in 1..daysInMonth) {
            val dayCalendar = monthCalendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
            dayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            dayCalendar.set(Calendar.MINUTE, 0)
            dayCalendar.set(Calendar.SECOND, 0)
            dayCalendar.set(Calendar.MILLISECOND, 0)

            addVisitGridCell(grid, buildVisitDayView(dayCalendar, today))
        }
    }

    private fun buildVisitDayView(dayCalendar: Calendar, today: Calendar): View {
        val inflater = LayoutInflater.from(requireContext())
        val dayView = inflater.inflate(R.layout.item_calendar_day, binding.gridVisitCalendarDays, false)

        val tvDay = dayView.findViewById<TextView>(R.id.tv_day)
        val viewDot = dayView.findViewById<View>(R.id.view_dot)

        tvDay.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()
        viewDot.visibility = View.INVISIBLE

        val isPast = dayCalendar.before(today)
        val dayKey = dayKeyFormat.format(dayCalendar.time)

        if (isPast) {
            dayView.alpha = 0.3f
            dayView.isClickable = false
            tvDay.isSelected = false
            return dayView
        }

        dayView.alpha = 1.0f
        tvDay.isSelected = selectedVisitDates.contains(dayKey)

        dayView.setOnClickListener {
            if (selectedVisitDates.contains(dayKey)) {
                selectedVisitDates.remove(dayKey)
            } else {
                selectedVisitDates.add(dayKey)
            }
            buildVisitCalendarGrid()
            updateSelectedDatesCountLabel()
            updateSaveButtonState()
        }

        return dayView
    }

    private fun addVisitGridCell(grid: GridLayout, view: View) {
        val params = GridLayout.LayoutParams()
        params.width = 0
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        view.layoutParams = params
        grid.addView(view)
    }

    private fun updateSelectedDatesCountLabel() {
    }

    private fun setupRoutesRecyclerView() {
        lineAdapter = LineAdapter(
            items = lines,
            getSelectedCode = { selectedLine?.LINE_CODE },
            onLineClick = { line ->
                selectedLine = line
                lineAdapter.updateList(currentFilteredList())
                updateRouteHeader()
                updateSaveButtonState()

                binding.cardCustomers.visibility = View.VISIBLE

                binding.contentRoute.visibility = View.GONE
                binding.ivChevronRoute.animate().rotation(0f).setDuration(200).start()

                binding.contentCustomers.visibility = View.VISIBLE
                binding.ivChevronCustomers.animate().rotation(180f).setDuration(200).start()

                getCustomers(line.LINE_CODE)
            }
        )
        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutes.adapter = lineAdapter

        binding.etSearchRoute.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lineAdapter.updateList(currentFilteredList(s?.toString().orEmpty()))
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun currentFilteredList(query: String = binding.etSearchRoute.text?.toString().orEmpty()): List<LineItem> {
        if (query.isBlank()) return lines
        return lines.filter {
            it.LINE_NAME.contains(query, ignoreCase = true) ||
                    it.LINE_CODE.contains(query, ignoreCase = true)
        }
    }

    private fun getSalesTypes() {
        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.GetSalesTypes)
    }

    private fun getLines(saleType: String) {
        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.GetLines(saleType))
    }

    private fun getCustomers(lineCode: String) {
        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.GetCustomers(lineCode))
    }

    private fun sendRefreshToken() {
        viewModel.addVisitPlanIntent.trySend(
            AddVisitIntent.RefreshToken(
                SharedPreferencesHelper.getInstance().getEmployeeId(),
                SharedPreferencesHelper.getInstance().getUserToken()
            )
        )
    }

    private fun handleResponse(
        code: Int,
        message: String,
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
                    message = message,
                    isSuccess = false,
                    showOkButton = true,
                )
            }
        }
    }

    private fun showSessionExpired(message: String) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message,
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
                        is AddVisitStatus.Loading -> {
                            binding.progressLoading.visibility = View.VISIBLE
                        }

                        is AddVisitStatus.GetSalesTypes -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getSalesTypes() }
                            ) {
                                salesTypes = status.response.data.sales_types
                                buildSaleTypeGrid()
                            }
                        }

                        is AddVisitStatus.GetLines -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { selectedSaleType?.let { getLines(it) } }
                            ) {
                                lines = status.response.data.lines
                                lineAdapter.updateList(currentFilteredList())
                            }
                        }

                        is AddVisitStatus.GetCustomers -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { selectedLine?.let { getCustomers(it.LINE_CODE) } }
                            ) {
                                customersList = status.response.data.setup_customers
                                selectedCustomerIds.clear()
                                customersAdapter.updateList(customersList)
                                updateCustomersCountLabel()
                                updateSaveButtonState()
                            }
                        }

                        is AddVisitStatus.SaveSetupPlan -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { submitPlan() }
                            ) {
                                Toast.makeText(requireContext(), status.response.message, Toast.LENGTH_SHORT).show()
                                if (status.response.data.success) {
                                    findNavController().popBackStack()
                                }
                            }
                        }

                        is AddVisitStatus.RefreshToken -> {
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

                        is AddVisitStatus.Error -> {
                            Log.d(TAG, "observeStatus: ${status.message}")
                            binding.progressLoading.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun buildSaleTypeGrid() {
        binding.gridSaleTypes.removeAllViews()
        binding.gridSaleTypes.columnCount = 2

        val inflater = LayoutInflater.from(requireContext())

        salesTypes.forEach { type ->
            val itemView = inflater.inflate(
                R.layout.item_sale_option_card,
                binding.gridSaleTypes,
                false
            ) as TextView

            itemView.text = type

            val isSelected = type == selectedSaleType
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_sale_type_selected)
                itemView.setTextColor(Color.WHITE)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_sale_type_unselected)
                itemView.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                selectedSaleType = type
                buildSaleTypeGrid()
                updateSaleTypeHeader()

                selectedLine = null
                lines = emptyList()
                binding.etSearchRoute.text?.clear()
                lineAdapter.updateList(emptyList())
                resetRouteHeader()

                customersList = emptyList()
                selectedCustomerIds.clear()
                customersAdapter.updateList(emptyList())
                updateCustomersCountLabel()

                updateSaveButtonState()

                binding.cardCustomers.visibility = View.GONE
                binding.cardRoute.visibility = View.VISIBLE

                binding.contentSaleType.visibility = View.GONE
                binding.ivChevronSaleType.animate().rotation(0f).setDuration(200).start()

                binding.contentRoute.visibility = View.VISIBLE
                binding.ivChevronRoute.animate().rotation(180f).setDuration(200).start()

                getLines(type)
            }

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(6, 6, 6, 6)
            itemView.layoutParams = params

            binding.gridSaleTypes.addView(itemView)
        }
    }

    private fun updateSaleTypeHeader() {
        val type = selectedSaleType ?: return
        binding.tvSaleTypeSelected.text = type
        binding.tvSaleTypeSelected.visibility = View.VISIBLE
        binding.tvStepNumberSaleType.visibility = View.GONE
        binding.ivCheckSaleType.visibility = View.VISIBLE
        binding.statusIndicatorSaleType.setCardBackgroundColor(Color.parseColor("#2ECC71"))
    }

    private fun updateRouteHeader() {
        val line = selectedLine ?: return
        binding.tvRouteSelected.text = "${line.LINE_NAME} (كود: ${line.LINE_CODE})"
        binding.tvRouteSelected.visibility = View.VISIBLE
        binding.tvStepNumberRoute.visibility = View.GONE
        binding.ivCheckRoute.visibility = View.VISIBLE
        binding.statusIndicatorRoute.setCardBackgroundColor(Color.parseColor("#2ECC71"))
    }

    private fun resetRouteHeader() {
        binding.tvRouteSelected.visibility = View.GONE
        binding.tvStepNumberRoute.visibility = View.VISIBLE
        binding.ivCheckRoute.visibility = View.GONE
        binding.statusIndicatorRoute.setCardBackgroundColor(Color.parseColor("#FFEEDD"))
    }

    private fun setupClickListeners() {
        binding.btnBackContainer.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSavePlan.setOnClickListener { submitPlan() }
    }

    private fun submitPlan() {
        val saleType = selectedSaleType
        val line = selectedLine

        if (saleType == null || line == null || selectedCustomerIds.isEmpty() || selectedVisitDates.isEmpty()) {
            Toast.makeText(requireContext(), "من فضلك أكمل كل الخطوات", Toast.LENGTH_SHORT).show()
            return
        }

        val customersRequest = customersList
            .filter { selectedCustomerIds.contains(it.PARTY_SITE_ID) }
            .map {
                SaveCustomerRequest(
                    customer_code = it.CUSTOMER_CODE,
                    party_site_id = it.PARTY_SITE_ID,
                    customer_type = it.CUSTOMER_PROFILE_CLASS,
                    customer_branch = it.CUSTOMER_BRANCH
                )
            }

        val request = SaveSetupPlanRequest(
            order_type = saleType,
            line_id = line.LINE_CODE,
            customers = customersRequest,
            dates = selectedVisitDates.sorted()
        )

        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.SaveSetupPlan(request))
    }

    private fun setupExpandableSections() {
        binding.headerSaleType.setOnClickListener {
            toggleSection(binding.contentSaleType, binding.ivChevronSaleType)
        }

        binding.headerRoute.setOnClickListener {
            toggleSection(binding.contentRoute, binding.ivChevronRoute)
        }

        binding.headerCustomers.setOnClickListener {
            toggleSection(binding.contentCustomers, binding.ivChevronCustomers)
        }
    }

    private fun toggleSection(contentView: View, chevronView: ImageView) {
        val isExpanded = contentView.visibility == View.VISIBLE

        if (isExpanded) {
            contentView.visibility = View.GONE
            chevronView.animate().rotation(0f).setDuration(200).start()
        } else {
            contentView.visibility = View.VISIBLE
            chevronView.animate().rotation(180f).setDuration(200).start()
        }
    }

    private fun setupCustomersRecyclerView() {
        customersAdapter = CustomersAdapter(
            list = customersList,
            isSelected = { selectedCustomerIds.contains(it.PARTY_SITE_ID) },
            onToggle = { item ->
                if (selectedCustomerIds.contains(item.PARTY_SITE_ID)) {
                    selectedCustomerIds.remove(item.PARTY_SITE_ID)
                } else {
                    selectedCustomerIds.add(item.PARTY_SITE_ID)
                }
                customersAdapter.refresh()
                updateCustomersCountLabel()
                updateSaveButtonState()
            }
        )
        binding.rvCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomers.adapter = customersAdapter

        binding.tvSelectAll.setOnClickListener {
            if (selectedCustomerIds.size == customersList.size) {
                selectedCustomerIds.clear()
            } else {
                selectedCustomerIds.clear()
                selectedCustomerIds.addAll(customersList.map { it.PARTY_SITE_ID })
            }
            customersAdapter.refresh()
            updateCustomersCountLabel()
            updateSaveButtonState()
        }
    }

    private fun updateCustomersCountLabel() {
        binding.tvStep2Title.text = "2. اختر العملاء (${selectedCustomerIds.size} محدد):"
    }

    private fun updateSaveButtonState() {
        val isReady = selectedSaleType != null &&
                selectedLine != null &&
                selectedCustomerIds.isNotEmpty() &&
                selectedVisitDates.isNotEmpty()

        binding.btnSavePlan.isEnabled = isReady
        binding.btnSavePlan.backgroundTintList = ColorStateList.valueOf(
            if (isReady) resources.getColor(R.color.colorPrimary, null)
            else Color.parseColor("#CCCCCC")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingRetry = null
        _binding = null
    }
}