package com.akhnaton.foodvisits.ui.home.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.databinding.BottomSheetProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProductBottomSheet(
    private val products: MutableList<Product>,
    private val listener: OnProductSelected
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetProductBinding

    private lateinit var adapter: ProductBottomSheetAdapter

    interface OnProductSelected {
        fun onSelected(product: Product)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = BottomSheetProductBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupKeyboardInsets()
        adapter = ProductBottomSheetAdapter(products) {
            listener.onSelected(it)
            dismiss()
        }

        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager =
            LinearLayoutManager(requireContext())
        binding.btnDone.setOnClickListener {
            dismiss()
        }

        binding.etSearch.addTextChangedListener {
            adapter.filter.filter(it.toString())
        }

    }

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(
                    imeInsets.bottom,
                    systemBars.bottom
                )
            )
            insets
        }
    }

}