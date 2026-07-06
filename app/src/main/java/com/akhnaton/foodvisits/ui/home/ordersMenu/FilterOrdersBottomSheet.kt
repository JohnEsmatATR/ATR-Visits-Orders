package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.akhnaton.foodvisits.data.model.getList.OrdersFilter
import com.akhnaton.foodvisits.databinding.BottomSheetFilterOrdersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class FilterOrdersBottomSheet(
    private val currentStatus: String,
    private val currentFromDate: String,
    private val currentToDate: String,
    private val currentOrderType: String,
    private val orderTypesList: List<String>,
    private val callback: (OrdersFilter) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetFilterOrdersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = BottomSheetFilterOrdersBinding.inflate(
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

        setupViews()

        setupDates()

        setupButtons()
    }

    private fun setupViews() {

        when (currentStatus) {

            "saved" -> binding.rbSaved.isChecked = true

            "sent" -> binding.rbSent.isChecked = true

            else -> binding.rbAll.isChecked = true
        }

        binding.etFromDate.setText(currentFromDate)

        binding.etToDate.setText(currentToDate)

        binding.etOrderType.setText(
            currentOrderType,
            false
        )

        val adapter =
            ArrayAdapter(
                requireContext(),
                R.layout.simple_list_item_1,
                orderTypesList
            )

        binding.etOrderType.setAdapter(adapter)
    }

    private fun setupDates() {

        binding.etFromDate.setOnClickListener {

            showDatePicker { date ->

                binding.etFromDate.setText(date)
            }
        }

        binding.etToDate.setOnClickListener {

            showDatePicker { date ->

                binding.etToDate.setText(date)
            }
        }
    }

    private fun setupButtons() {

        binding.btnApply.setOnClickListener {

            val status =
                when {

                    binding.rbSaved.isChecked -> "saved"

                    binding.rbSent.isChecked -> "sent"

                    else -> ""
                }

            callback.invoke(
                OrdersFilter(
                    status = status,
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

    private fun showDatePicker(
        onDateSelected: (String) -> Unit
    ) {

        val calendar = Calendar.getInstance()

        val picker =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    val formattedDate =
                        String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                        )

                    onDateSelected(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

        picker.show()
    }
}