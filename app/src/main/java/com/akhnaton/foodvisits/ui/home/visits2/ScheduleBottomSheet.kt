package com.akhnaton.foodvisits.ui.home.visits2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.getSalesMan.SalesMan
import com.akhnaton.foodvisits.databinding.BottomSheetScheduleBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.filter
import kotlin.collections.toMutableList

class ScheduleBottomSheet(
    private val employees: List<SalesMan>,
    private val listener: Listener
) : BottomSheetDialogFragment() {

    interface Listener {

        fun onConfirm(
            employee: SalesMan,
            date: String
        )
    }

    private lateinit var binding: BottomSheetScheduleBinding

    private lateinit var adapter: RepsAdapter

    private var selectedDate = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = BottomSheetScheduleBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = RepsAdapter(employees.toMutableList())

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        // today's date

        val calendar = Calendar.getInstance()

        selectedDate =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(calendar.time)

        binding.calendarView.date = calendar.timeInMillis

        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            selectedDate = "%02d-%02d-%04d".format(
                day,
                month + 1,
                year
            )
        }

        val minDate = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val maxDate = calendar.timeInMillis

        binding.calendarView.apply {
            date = minDate
            this.minDate = minDate
            this.maxDate = maxDate
        }

        binding.etSearch.addTextChangedListener {
            val text = it.toString()
            adapter.filter(
                employees.filter {
                    it.SALES_MAN.contains(
                        text,
                        true
                    )
                }
            )
        }

        binding.btnSave.setOnClickListener {

            val employee =
                adapter.getSelected() ?: return@setOnClickListener

            listener.onConfirm(
                employee,
                selectedDate
            )

            dismiss()
        }
    }
}