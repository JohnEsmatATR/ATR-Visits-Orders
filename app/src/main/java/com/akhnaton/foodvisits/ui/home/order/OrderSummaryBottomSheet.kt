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
    private val beforeTax: String? = null,
    private val tax: String? = null,
    private val afterTax: String? = null,
    private val total: String? = null,
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
        if (beforeTax == null)
            binding.llBeforeTax.visibility = View.GONE
        else
            binding.tvBeforeTax.text = "${beforeTax} ${context?.getString(R.string.currency)}"

        if (tax == null)
            binding.llTax.visibility = View.GONE
        else
            binding.tvTax.text = "$tax ${context?.getString(R.string.currency)}"

        if (afterTax == null)
            binding.llAfterTax.visibility = View.GONE
        else
            binding.tvAfterTax.text = "$afterTax ${context?.getString(R.string.currency)}"

        if (total == null)
            binding.llTotal.visibility = View.GONE
        else
            binding.tvTotal.text = "$total ${context?.getString(R.string.currency)}"

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