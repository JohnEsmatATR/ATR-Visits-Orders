package com.akhnaton.foodvisits.ui.home.visits2

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.getSalesMan.SalesMan
import com.akhnaton.foodvisits.databinding.BottomSheetScheduleBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleBottomSheet(
    private val employees: List<SalesMan>,
    private val listener: Listener
) : BottomSheetDialogFragment() {

    interface Listener {

        fun onConfirm(
            employee: SalesMan,
            date: String,
            targetDate: String,
        )
    }

    private lateinit var binding: BottomSheetScheduleBinding

    private lateinit var adapter: RepsAdapter

    private var selectedDate = ""
    private var selectedTargetDate = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = BottomSheetScheduleBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupEmployees()
        setupDate()
        setupSearch()
        setupSaveButton()
    }

    private fun setupEmployees() {

        adapter = RepsAdapter(
            employees.toMutableList()
        )

        binding.rvUsers.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvUsers.adapter = adapter
    }

    private fun setupDate() {

        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.ENGLISH
        )

        selectedDate = dateFormat.format(calendar.time)
        selectedTargetDate = dateFormat.format(calendar.time)

        binding.etDate.setText(selectedDate)
        binding.etTargetDate.setText(selectedTargetDate)

        binding.etDate.setOnClickListener {
            showDatePicker("date")
        }

        binding.etTargetDate.setOnClickListener {
            showDatePicker("targetDate")
        }

        binding.cardDate.setOnClickListener {
            showDatePicker("date")
        }

        binding.cardTargetDate.setOnClickListener {
            showDatePicker("targetDate")
        }
    }

    private fun showDatePicker(date: String) {
        // Today
        val today = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                if (date == "date") {
                    selectedDate = SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.ENGLISH
                    ).format(selectedCalendar.time)
                    binding.etDate.setText(selectedDate)
                }
                else if (date == "targetDate") {
                    selectedTargetDate = SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.ENGLISH
                    ).format(selectedCalendar.time)
                    binding.etTargetDate.setText(selectedTargetDate)
                }
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        val minDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val maxDate = Calendar.getInstance().apply {
            set(
                Calendar.DAY_OF_MONTH,
                getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        datePickerDialog.datePicker.minDate = minDate.timeInMillis
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
        datePickerDialog.show()
    }

    private fun setupSearch() {

        binding.etSearch.addTextChangedListener {

            val text = it.toString()

            adapter.filter(
                employees.filter { employee ->

                    employee.SALES_MAN.contains(
                        text,
                        true
                    )
                }
            )
        }
    }

    private fun setupSaveButton() {

        binding.btnSave.setOnClickListener {

            val employee =
                adapter.getSelected()

            if (employee == null) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "يجب اختيار المندوب أولا",
                    isSuccess = false,
                    showOkButton = true
                )
                return@setOnClickListener
            }

            listener.onConfirm(
                employee,
                selectedDate,
                selectedTargetDate
            )

            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }
}