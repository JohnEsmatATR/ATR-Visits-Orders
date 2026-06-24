package com.akhnaton.foodvisits.ui.home.order

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.databinding.FragmentInvoiceBinding
import com.akhnaton.foodvisits.databinding.FragmentOrderCreationCycleBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.order.products.ProductAdapter
import com.akhnaton.foodvisits.ui.home.order.OrderCreationCycleAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.getValue

class InvoiceFragment : Fragment() {

    companion object {
        private const val TAG = "InvoiceFragment"
    }

    private val viewModel: Order2ViewModel by viewModels()
    private lateinit var binding: FragmentInvoiceBinding
    private lateinit var dialog: AlertDialog

    lateinit var customerName: String
    lateinit var customerCode: String
    lateinit var siteAddress: String
    lateinit var customerPartySiteId: String
    lateinit var saleType: String
    lateinit var json: String

    private var isOrderTab = true
    private var isSelectedTab = false

    private var allProducts = mutableListOf<Product>()

    private val orderSelections =
        mutableMapOf<String, Int>()

    private val returnSelections =
        mutableMapOf<String, Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerName =
            arguments?.getString("customerName").toString()
        customerCode =
            arguments?.getString("customerCode").toString()
        siteAddress =
            arguments?.getString("siteAddress").toString()
        customerPartySiteId =
            arguments?.getString("customerPartySiteId").toString()
        saleType =
            arguments?.getString("saleType").toString()
        json =
            arguments?.getString("selectedOptions").toString()

        val type = object :
            TypeToken<MutableList<SelectLists>>() {}.type

        val selectedOptions: MutableList<SelectLists> =
            Gson().fromJson(json, type)

//        selectedOptions.forEach {
//            Log.d(
//                "SELECTED",
//                "${it.select_name} = ${it.selectedValue}"
//            )
//        }
//
//        val paymentTerm =
//            selectedOptions.firstOrNull {
//                it.select == "payment_terms"
//            }
//
//        Log.d(
//            "PAYMENT",
//            paymentTerm?.selectedId ?: ""
//        )

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        binding.tabAll.setOnClickListener {
            isSelectedTab = false
            updateFilterTabs()
            updateScreen()
        }

        binding.tabSelected.setOnClickListener {
            isSelectedTab = true
            updateFilterTabs()
            updateScreen()
        }

        binding.tabOrder.setOnClickListener {
            isOrderTab = true
            updateOrderTabs()
            updateScreen()
        }

        binding.tabReturn.setOnClickListener {
            isOrderTab = false
            updateOrderTabs()
            updateScreen()
        }

        getStartOrderData()
        fetchData()
    }

    private fun updateFilterTabs() {
        if (isSelectedTab) {
            binding.tabSelected.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabAll.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
        } else {
            binding.tabAll.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabSelected.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
        }
    }

    private fun updateOrderTabs() {
        if (isOrderTab) {
            binding.tabOrder.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabReturn.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
        } else {
            binding.tabReturn.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabOrder.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
        }
    }

    private fun getStartOrderData() {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.GetStartOrderData(
                    customerPartySiteId,
                    saleType,
                    customerCode
                )
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Order2Status.Idle -> {}
                    is Order2Status.Loading -> dialog.show()

                    is Order2Status.GetStartOrderData -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            setRecycler(it.data.data.products.toMutableList())
                            binding.tvInvoice.setText(it.data.data.invoice_number)
                            allProducts = it.data.data.products.toMutableList()
                            updateScreen()
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.orderIntent.send(
                                    Order2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        }
                    }

                    is Order2Status.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
//                                )
//                            getData()
                        } else {

                        }
//                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is Order2Status.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()

//                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setRecycler(
        list: MutableList<Product>
    ) {
        val adapter =
            ProductAdapter(
                list,
                currentSelections(),
                object : ProductAdapter.OnItemActionListener {

                    override fun onItemClicked(
                        item: Product
                    ) {
                    }

                    override fun onQuantityChanged(
                        item: Product,
                        qty: Int
                    ) {
                        val selections =
                            currentSelections()
                        if (qty > 0) {
                            selections[item.ITEM_CODE] = qty
                        } else {
                            selections.remove(item.ITEM_CODE)
                        }

                        calculateTotals()

                        if (isSelectedTab) {
                            updateScreen()
                        }
                    }
                }
            )

        binding.rvProducts.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rvProducts.adapter = adapter
        binding.rvProducts.itemAnimator = DefaultItemAnimator()
    }

    private fun calculateTotals() {
        val selections =
            currentSelections()
        var totalQty = 0
        var beforeTax = 0.0
        allProducts.forEach { product ->
            val qty =
                selections[product.ITEM_CODE] ?: 0
            totalQty += qty
            beforeTax +=
                qty * product.ITEM_PRICE.toDouble()
        }

        val tax =
            beforeTax * 0.14
        val afterTax =
            beforeTax + tax
        binding.tvSelectedCount.text =
            totalQty.toString()
        binding.tvBeforeTax.text =
            "%.2f".format(beforeTax)
        binding.tvTax.text =
            "%.2f".format(tax)
        binding.tvAfterTax.text =
            "%.2f".format(afterTax)
    }

    private fun updateScreen() {
        val selections =
            currentSelections()
        val displayedList =
            if (isSelectedTab) {
                allProducts.filter {
                    selections.containsKey(
                        it.ITEM_CODE
                    )
                }.toMutableList()
            } else {
                allProducts
            }
        setRecycler(displayedList)
        binding.layoutSummary.visibility =
            if (isSelectedTab)
                View.VISIBLE
            else
                View.GONE
        calculateTotals()
    }

    private fun currentSelections(): MutableMap<String, Int> {
        return if (isOrderTab)
            orderSelections
        else
            returnSelections
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_invoice,
                container,
                false
            )
        return binding.root
    }

}