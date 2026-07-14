package com.akhnaton.foodvisits.ui.home.order

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.databinding.FragmentInvoice2Binding
import com.akhnaton.foodvisits.databinding.FragmentInvoiceBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.products.ProductAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.getValue

class InvoiceFragment2 : Fragment() {

    companion object {
        private const val TAG = "InvoiceFragment"
    }

    private val viewModel: Order2ViewModel by viewModels()
    private lateinit var binding: FragmentInvoice2Binding
    private lateinit var dialog: AlertDialog

    lateinit var customerName: String
    lateinit var customerCode: String
    lateinit var siteAddress: String
    lateinit var customerPartySiteId: String
    lateinit var saleType: String
    lateinit var selectedOptionsJson: String
    private var allProducts = mutableListOf<Product>()

    private val orderSelections = mutableMapOf<String, Int>()

    lateinit var orderId: String
    var paymentId: String? = ""
    var warehouseId: String? = ""
    var saleTypeId: String? = ""
    private var displayedProducts = mutableListOf<Product>()

    private var isEdit: Boolean? = false
    lateinit var OrigSysDocumentRef: String
    lateinit var orderJson: String
    private var getItemsResponse = mutableListOf<com.akhnaton.foodvisits.data.model.getItems.Data>()
    lateinit var orderData: com.akhnaton.foodvisits.data.model.getList.Data
    private var isSend: Boolean? = false
    lateinit var priceListId: String
    lateinit var storeId: String

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var selectedProduct: Product? = null

    private lateinit var orderAdapter: ProductAdapter

    var selections: MutableMap<String, Int>? = null
    var totalQty = 0
    var beforeTax = 0.0

    var tax = 0
    var afterTax = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        isEdit = arguments?.getBoolean("isEdit", false)
        if (!isEdit!!) {
            customerName = arguments?.getString("customerName").toString()
            customerCode = arguments?.getString("customerCode").toString()
            siteAddress = arguments?.getString("siteAddress").toString()
            customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
            saleType = arguments?.getString("saleType").toString()
            selectedOptionsJson = arguments?.getString("selectedOptions").toString()

            val type = object : TypeToken<List<SelectedOption>>() {}.type
            val selectedOptions: List<SelectedOption> = Gson().fromJson(
                selectedOptionsJson, type
            )
            paymentId = selectedOptions.firstOrNull {
                it.key == "payment_terms"
            }?.id ?: "106014"
            warehouseId = selectedOptions.firstOrNull {
                it.key == "warehouse_type"
            }?.id
            saleTypeId = selectedOptions.firstOrNull {
                it.key == "sale_type"
            }?.id
            Log.d("WHATinvoice", paymentId.toString())
            Log.d("WHATinvoice", warehouseId.toString())
            Log.d("WHATinvoice", saleTypeId.toString())

        } else {
            OrigSysDocumentRef = arguments?.getString("OrigSysDocumentRef").toString()
            orderJson = arguments?.getString("orderJson").toString()
            orderData = Gson().fromJson(
                orderJson, com.akhnaton.foodvisits.data.model.getList.Data::class.java
            )
            paymentId = orderData.PAYMENT_TERM_ID
//            paymentId = "106014"
            customerCode = orderData.CUSTOMER_CODE
            customerPartySiteId = orderData.PARTY_SITE_ID
            saleType = orderData.ORDER_TYPE

            getItems()

            Log.d("WHAT", orderJson.toString())
            Log.d("WHAT", orderData.toString())
        }

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        MainActivity.binding.navView2.visibility = View.GONE

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "هل انت متأكد انك تريد الرجوع ؟",
                    isSuccess = true,
                    showYesNoButtons = true,
                    onYes = {
                        findNavController().popBackStack(
                            R.id.visitPhoneFragment, false
                        )
                    })
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backPressedCallback
        )

        orderAdapter = ProductAdapter(
            displayedProducts,
            currentSelections(),
            object : ProductAdapter.OnItemActionListener {

                override fun onItemClicked(
                    item: Product
                ) {
                }

                override fun onQuantityChanged(
                    item: Product,
                    qty: Int,
                    position: Int
                ) {
                    selections = currentSelections()
                    if (qty > 0)
                        selections?.set(item.ITEM_CODE, qty)
                    else
                        selections?.remove(item.ITEM_CODE)
                    calculateTotals()
                    // Only call API first time
                    if (!item.clicked) {
                        item.clicked = true
//                        getItemDetails(
//                            item.INVENTORY_ITEM_ID,
//                            priceListId,
//                            storeId
//                        )
                    }
                }

                override fun onDeleteClicked(item: Product) {
                    selections = currentSelections()
                    selections?.remove(item.ITEM_CODE)
                    calculateTotals()
                    updateScreen()
                }
            })

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }

        binding.cardBottom.setOnClickListener {
            calculateTotals()
            OrderSummaryBottomSheet(
                selectedCount = currentSelections().size.toString(),
                beforeTax = beforeTax.toString(),
                tax = tax.toString(),
                afterTax = afterTax.toString(),
                listener = object : OrderSummaryBottomSheet.Listener {

                    override fun onSave() {
                        // Save order
                    }

                    override fun onSend() {
                        // Send order
                    }
                }
            ).show(parentFragmentManager, "OrderSummary")
        }

        binding.btnBack.setOnClickListener {
            DialogUtils.showResultDialog(
                context = requireContext(),
                message = "هل انت متأكد انك تريد الرجوع ؟",
                isSuccess = true,
                showYesNoButtons = true,
                onYes = {
                    findNavController().popBackStack(
                        R.id.visitPhoneFragment, false
                    )
                })
        }

        binding.searchLayout.setOnClickListener {
            ProductBottomSheet(
                allProducts,
                object : ProductBottomSheet.OnProductSelected {
                    override fun onSelected(product: Product) {
                        binding.etSearch.setText(product.PRODUCT_NAME)
                        selectedProduct = product
                        getItemDetails(
                            product.INVENTORY_ITEM_ID,
                            priceListId,
                            storeId
                        )
                        binding.layoutAdd.visibility = View.VISIBLE
                    }
                }
            ).show(parentFragmentManager, "products")
        }

        binding.etSearch.setOnClickListener {
            ProductBottomSheet(
                allProducts,
                object : ProductBottomSheet.OnProductSelected {
                    override fun onSelected(product: Product) {
                        binding.etSearch.setText(product.PRODUCT_NAME)
                        selectedProduct = product
                        getItemDetails(
                            product.INVENTORY_ITEM_ID,
                            priceListId,
                            storeId
                        )
                        binding.layoutAdd.visibility = View.VISIBLE
                    }
                }
            ).show(parentFragmentManager, "products")
        }

        binding.btnPlus.setOnClickListener {
            if (binding.etQty.text.isNotEmpty()) {
                binding.etQty.setText((binding.etQty.text.toString().toInt() + 1).toString())
            } else {
                binding.etQty.setText("1")
            }
        }

        binding.btnMinus.setOnClickListener {
            if (binding.etQty.text.isNotEmpty() && binding.etQty.text.toString().toInt() > 1) {
                binding.etQty.setText((binding.etQty.text.toString().toInt() - 1).toString())
            } else {
                binding.etQty.setText("1")
            }
        }

        binding.btnAdd.setOnClickListener {
            val product = selectedProduct ?: return@setOnClickListener
            val qty =
                binding.etQty.text.toString().toIntOrNull() ?: 1
            val currentQty =
                orderSelections[product.ITEM_CODE] ?: 0
            orderSelections[product.ITEM_CODE] =
                currentQty + qty
            updateScreen()
            updateTotal()
            binding.etSearch.text?.clear()
            binding.etQty.text?.clear()
            binding.layoutAdd.visibility = View.GONE
            selectedProduct = null
        }
        getStartOrderData()
        fetchData()
    }

    private fun updateEmptyView() {
        if (displayedProducts.isEmpty()) {
            binding.llZeroState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.llZeroState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
        }
    }

    private fun updateTotal() {
        var total = 0.0
        allProducts.forEach {

            val qty = orderSelections[it.ITEM_CODE] ?: 0

            total += it.CUST_PRICE.toDouble() * qty
        }
        binding.tvTotal.text =
            String.format("%.2f ج.م", total)
    }

    private fun getStartOrderData() {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.GetStartOrderData(
                    customerPartySiteId, saleType, customerCode
                )
            )
        }
    }

    private fun getItems() {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.GetItems(
                    OrigSysDocumentRef
                )
            )
        }
    }

    private fun prefillOrderSelections() {
        Log.d("MATCH getItemsResponse", getItemsResponse.size.toString())
        Log.d("MATCH allProducts", allProducts.size.toString())
        getItemsResponse.forEach { preview ->
            Log.d(
                "MATCH", "Searching for: ${preview.ITEM_NAME}"
            )
            val matchedProduct = allProducts.firstOrNull {
                it.PRODUCT_NAME.trim().replace("'", "") == preview.ITEM_NAME.trim().replace("'", "")
            }
            Log.d(
                "MATCH", "Found: ${matchedProduct?.PRODUCT_NAME}"
            )
            matchedProduct?.let {
                orderSelections[it.ITEM_CODE] = preview.QUANTITY.toInt()
            }
        }
    }

    private fun updateScreen() {
        filterProducts(binding.etSearch.text.toString())
        binding.llZeroState.visibility =
            if (displayedProducts.isEmpty()) View.VISIBLE
            else View.GONE
        binding.rvProducts.visibility =
            if (displayedProducts.isEmpty()) View.GONE
            else View.VISIBLE
        calculateTotals()
    }

    private fun currentSelections(): MutableMap<String, Int> {
        return orderSelections
    }

    private fun filterProducts(
        keyword: String
    ) {

        val baseList = allProducts.filter {
            (currentSelections()[it.ITEM_CODE] ?: 0) > 0
        }

        val filtered = if (keyword.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.PRODUCT_NAME.contains(keyword, true) ||
                        it.ITEM_CODE.contains(keyword, true)
            }
        }

        setRecycler(filtered.toMutableList())

        binding.llZeroState.visibility = if (displayedProducts.isEmpty()) View.VISIBLE
        else View.GONE
    }

    private fun setRecycler(list: MutableList<Product>) {

        displayedProducts.clear()

        displayedProducts.addAll(list)

        orderAdapter.notifyDataSetChanged()
    }

    private fun calculateTotals() {
        selections = currentSelections()
        totalQty = 0
        beforeTax = 0.0
        allProducts.forEach { product ->
            val qty = selections?.get(product.ITEM_CODE) ?: 0
            totalQty += qty
            beforeTax += qty * product.ITEM_PRICE.toDouble()
        }

        val tax = beforeTax * 0.14
        val afterTax = beforeTax + tax
    }

    private fun getItemDetails(itemId: String, priceList: String, storeId: String) {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.GetItemDetails(
                    itemId, priceList, storeId
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
                            val data = Gson().fromJson(
                                it.data.data, Data::class.java
                            )
                            orderId = data.invoice_number
                            priceListId = data.price_list_id
                            storeId = data.store_id
                            val ltrNumber = "\u200E${data.invoice_number}\u200E"

                            binding.tvInvoice.text = "${getString(R.string.invoice)}: $ltrNumber"

                            allProducts = data.products.toMutableList()

                            if (isEdit == true) {
                                prefillOrderSelections()
                            }

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
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message, false,
                                showOkButton = true,
                            )
                        }
                    }

                    is Order2Status.SaveOrder -> {
                        dialog.dismiss()
                        if (it.saveOrderRes.status == 200) {
                            val data = Gson().fromJson(
                                it.saveOrderRes.data,
                                com.akhnaton.foodvisits.data.model.saveOrder.Data::class.java
                            )
                            if (isSend == false) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.saveOrderRes.message.firstOrNull().orEmpty(),
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                    })
                            } else if (isSend == true) {
                                MainActivity.binding.navView2.visibility = View.VISIBLE
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.saveOrderRes.message.firstOrNull().orEmpty(),
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                        findNavController().navigate(
                                            R.id.toHome,
                                            null,
                                            androidx.navigation.NavOptions.Builder().setPopUpTo(
                                                findNavController().graph.startDestinationId, true
                                            ).build()
                                        )
                                    })
                            }

                        } else if (it.saveOrderRes.status == 401) {
                            lifecycleScope.launch {
                                viewModel.orderIntent.send(
                                    Order2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(),
                                it.saveOrderRes.message.firstOrNull().orEmpty(),
                                false,
                                showOkButton = true,
                            )
                        }
                    }

                    is Order2Status.EditOrder -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.editOrder.Data::class.java
                            )
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message[0],
                                isSuccess = true,
                                seconds = 2,
                                showOkButton = true,
                            )
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.orderIntent.send(
                                    Order2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message[0], false,
                                showOkButton = true,
                            )
                        }
                    }

                    is Order2Status.GetItems -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            getItemsResponse = it.data.data.toMutableList()
                            prefillOrderSelections()
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.orderIntent.send(
                                    Order2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message, false,
                                showOkButton = true,
                            )
                        }
                    }

                    is Order2Status.GetItemDetails -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val product = allProducts.firstOrNull() { pro ->
                                pro.INVENTORY_ITEM_ID == it.data.data.INVENTORY_ITEM_ID
                            }
                            product?.TOTAL_QUANTITY = it.data.data.QUANTITY.toInt()
                            binding.etQty.setHint(product?.TOTAL_QUANTITY.toString())
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.orderIntent.send(
                                    Order2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message, false,
                                showOkButton = true,
                            )
                        }
                    }

                    is Order2Status.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            Log.d("WHATRefreshToken", "${it.data.message}")
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                            )
                            SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
                                    SharedPreferencesHelper.getInstance().logOut()
                                    startActivity(
                                        Intent(
                                            requireContext(), LoginActivity2::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                })
                        }
                    }

                    is Order2Status.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()
                        DialogUtils.showResultDialog(
                            context = requireContext(),
                            message = "خطأ",
                            isSuccess = true,
                            showOkButton = true,
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_invoice2, container, false
        )
        return binding.root
    }
}