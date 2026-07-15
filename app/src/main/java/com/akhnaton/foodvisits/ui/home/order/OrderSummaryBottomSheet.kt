package com.akhnaton.foodvisits.ui.home.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.BottomSheetOrderSummaryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderSummaryBottomSheet(
    private val selectedCount: String,
    private val beforeTax: String,
    private val tax: String,
    private val afterTax: String,
    private val listener: Listener
) : BottomSheetDialogFragment() {

    interface Listener {
        fun onSave()
        fun onSend()
    }

    private var _binding: BottomSheetOrderSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetOrderSummaryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.tvSelectedCount.text = "${selectedCount} صنف"
        binding.tvBeforeTax.text = "${beforeTax} ${context?.getString(R.string.currency)}"
        binding.tvTax.text = "$tax ${context?.getString(R.string.currency)}"
        binding.tvAfterTax.text = "$afterTax ${context?.getString(R.string.currency)}"

        binding.btnSave.setOnClickListener {
            listener.onSave()
            dismiss()
        }

        binding.btnSend.setOnClickListener {
            listener.onSend()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}