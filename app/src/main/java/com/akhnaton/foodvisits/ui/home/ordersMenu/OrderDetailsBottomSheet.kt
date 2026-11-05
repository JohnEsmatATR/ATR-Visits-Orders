package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getItems.Data
import com.akhnaton.foodvisits.data.model.getList.ITEMSPREVIEW
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderDetailsBottomSheet(
    private val products: List<Data>,
    private val order: com.akhnaton.foodvisits.data.model.getList.Data?
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

        val rv = view.findViewById<RecyclerView>(R.id.rvProducts)

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ProductPreviewAdapter(products)

        view.findViewById<View>(R.id.ivClose).setOnClickListener { dismiss() }

        order?.let {
            view.findViewById<TextView>(R.id.tvCustomerName).text = it.CUSTOMER_NAME
            view.findViewById<TextView>(R.id.tvCustomerCode).text = "كود العميل: ${it.CUSTOMER_CODE}"
            view.findViewById<TextView>(R.id.tvInvoiceNumber).text = "رقم الفاتورة: ${it.ORIG_SYS_DOCUMENT_REF}"
            view.findViewById<TextView>(R.id.tvDate).text = "التاريخ: ${it.ORDER_DATE}"
            view.findViewById<TextView>(R.id.tvOrderType).text = "النوع: ${it.ORDER_TYPE}"
            view.findViewById<TextView>(R.id.tvTotalQty).text = "${it.TOTAL_QUANTITY} قطعة"
            view.findViewById<TextView>(R.id.tvTotalValue).text = "${it.TOTAL_VALUE} ج.م"
        }
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