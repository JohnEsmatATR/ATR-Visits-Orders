package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.akhnaton.foodvisits.data.model.getList.OrdersFilter
import com.akhnaton.foodvisits.databinding.BottomSheetFilterOrdersBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import java.util.Locale

class FilterOrdersBottomSheet(
    private val currentStatus: String,
    private val currentFromDate: String,
    private val currentToDate: String,
    private val currentOrderType: String,
    private val orderTypesList: List<String>,
    private val callback: (OrdersFilter) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetFilterOrdersBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetFilterOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupDates()
        setupButtons()
    }

    private fun setupViews() {
        binding.etFromDate.setText(currentFromDate)
        binding.etToDate.setText(currentToDate)
        binding.etOrderType.setText(currentOrderType, false)

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_list_item_1,
            orderTypesList
        )

        binding.etOrderType.setAdapter(adapter)
    }



    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            callback.invoke(
                OrdersFilter(
                    status = "",
                    fromDate = binding.etFromDate.text.toString(),
                    toDate = binding.etToDate.text.toString(),
                    orderType = binding.etOrderType.text.toString()
                )
            )
            dismiss()
        }

        binding.btnClear.setOnClickListener {
            callback.invoke(
                OrdersFilter(
                    status = "",
                    fromDate = "",
                    toDate = "",
                    orderType = ""
                )
            )
            dismiss()
        }
    }

    private val todayEnd: Calendar
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

    private fun parseDate(value: String): Calendar? {
        val parts = value.split("-")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return Calendar.getInstance().apply {
            set(y, m - 1, d, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun setupDates() {
        binding.etFromDate.setOnClickListener {
            val to = parseDate(binding.etToDate.text.toString())
            showDatePicker(
                initial = binding.etFromDate.text.toString(),
                minMillis = null,
                maxMillis = (to ?: todayEnd).timeInMillis.coerceAtMost(todayEnd.timeInMillis)
            ) { date ->
                binding.etFromDate.setText(date)
                val currentTo = parseDate(binding.etToDate.text.toString())
                val newFrom = parseDate(date)
                if (currentTo != null && newFrom != null && currentTo.before(newFrom)) {
                    binding.etToDate.setText("")
                }
            }
        }

        binding.etToDate.setOnClickListener {
            val from = parseDate(binding.etFromDate.text.toString())
            showDatePicker(
                initial = binding.etToDate.text.toString(),
                minMillis = from?.timeInMillis,
                maxMillis = todayEnd.timeInMillis
            ) { date -> binding.etToDate.setText(date) }
        }
    }

    private fun showDatePicker(
        initial: String,
        minMillis: Long?,
        maxMillis: Long,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = (parseDate(initial) ?: Calendar.getInstance()).apply {
            var t = timeInMillis.coerceAtMost(maxMillis)
            if (minMillis != null) t = t.coerceAtLeast(minMillis)
            timeInMillis = t
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                onDateSelected(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minMillis?.let { datePicker.minDate = it }
            datePicker.maxDate = maxMillis
        }.show()
    }
}