package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getList.ITEMSPREVIEW
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderDetailsBottomSheet(
    private val products: List<ITEMSPREVIEW>
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.bottom_sheet_order_details,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        val rv =
            view.findViewById<RecyclerView>(R.id.rvProducts)

        rv.layoutManager =
            LinearLayoutManager(requireContext())

        rv.adapter =
            ProductPreviewAdapter(products)
    }

    override fun onStart() {
        super.onStart()

        dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.setBackgroundResource(android.R.color.transparent)

        dialog?.let { dialog ->
            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )

            bottomSheet?.layoutParams?.height =
                ViewGroup.LayoutParams.MATCH_PARENT
        }
    }
}