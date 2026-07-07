package com.akhnaton.foodvisits.ui.home.order

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderItemReq
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderReq
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderItemReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentInvoiceBinding
import com.akhnaton.foodvisits.databinding.FragmentOrderCreationCycleBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.products.ProductAdapter
import com.akhnaton.foodvisits.ui.home.order.OrderCreationCycleAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlin.collections.mutableListOf
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
    lateinit var selectedOptionsJson: String

    private var isOrderTab = true
    private var isSelectedTab = false

    private var allProducts = mutableListOf<Product>()

    private val orderSelections = mutableMapOf<String, Int>()
    private val returnSelections = mutableMapOf<String, Int>()

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

    private var selectedItemCode: String? = null

    private var pendingItem: Product? = null
    private var pendingQty: Int = 0
    private var isSearchVisible: Boolean? = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tabSelected.setText(
            "${requireContext().getString(R.string.selected)} (${currentSelections().size})"
        )

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

        binding.ivSearch.setOnClickListener {
            isSearchVisible = !isSearchVisible!!
            if (isSearchVisible == true) binding.etSearch.visibility = View.VISIBLE
            if (isSearchVisible == false) binding.etSearch.visibility = View.GONE
        }

        binding.etSearch.addTextChangedListener {
            val keyword = it.toString().trim()
            filterProducts(keyword)
        }

        binding.btnSave.setOnClickListener {
            isSend = false
            if (!isEdit!!) {
                val selections = currentSelections()

                val itemsMap = selections.entries.mapIndexed { index, entry ->

                    val product = allProducts.first {
                        it.ITEM_CODE == entry.key
                    }

                    index.toString() to SaveOrderItemReq(
                        inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),

                        quantity = entry.value
                    )
                }.toMap()
                val request = SaveOrderReq(
                    orderId = orderId,
                    partySiteId = customerPartySiteId,
                    orderType = saleType,
                    deviceType = "Android",
                    send = "0",
                    warehouseType = warehouseId.toString(),
//                        login = SharedPreferencesHelper
//                            .getInstance()
//                            .getEmployeeId()
//                            .toInt(),
                    paymentId = paymentId.toString().toInt(),
                    items = itemsMap
                )

                Log.d("WHATrequest", request.toString())

                lifecycleScope.launch {
                    viewModel.orderIntent.send(
                        Order2Intent.SaveOrder(
                            saveOrderReq = request
                        )
                    )
                }
            } else if (isEdit!!) {
                val selections = currentSelections()

                val itemsMap = selections.entries.mapIndexed { index, entry ->

                    val product = allProducts.first {
                        it.ITEM_CODE == entry.key
                    }

                    index.toString() to EditOrderItemReq(
                        inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),

                        quantity = entry.value
                    )
                }.toMap()

                val editOrderReq = EditOrderReq(
                    orderId = OrigSysDocumentRef, items = itemsMap
                )

                lifecycleScope.launch {
                    viewModel.orderIntent.send(
                        Order2Intent.EditOrder(
                            editOrderReq = editOrderReq
                        )
                    )
                }
            }
        }

        binding.btnSend.setOnClickListener {
            isSend = true
            val selections = currentSelections()

            val itemsMap = selections.entries.mapIndexed { index, entry ->

                val product = allProducts.first {
                    it.ITEM_CODE == entry.key
                }

                index.toString() to SaveOrderItemReq(
                    inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),

                    quantity = entry.value
                )
            }.toMap()
            val request = SaveOrderReq(
                orderId = orderId,
                partySiteId = customerPartySiteId,
                orderType = saleType,
                deviceType = "Android",
                send = "1",
                warehouseType = warehouseId.toString(),
//                    login = SharedPreferencesHelper
//                        .getInstance()
//                        .getEmployeeId()
//                        .toInt(),
                paymentId = paymentId.toString().toInt(),
                items = itemsMap
            )

            Log.d("WHATrequest", request.toString())

            lifecycleScope.launch {
                viewModel.orderIntent.send(
                    Order2Intent.SaveOrder(
                        saveOrderReq = request
                    )
                )
            }
        }

        getStartOrderData()
        fetchData()
    }

    private fun updateFilterTabs() {
        if (isSelectedTab) {
            binding.tabSelected.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabSelected.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabAll.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
            binding.tabAll.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unselected_tab_text_color)
            )
        } else {
            binding.tabAll.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabAll.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabSelected.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
            binding.tabSelected.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unselected_tab_text_color)
            )
        }
    }

    private fun updateOrderTabs() {
        if (isOrderTab) {
            binding.tabOrder.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabOrder.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabReturn.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
            binding.tabReturn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unselected_tab_text_color)
            )
        } else {
            binding.tabReturn.setBackgroundResource(
                R.drawable.bg_tab_selected
            )
            binding.tabReturn.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            binding.tabOrder.setBackgroundResource(
                R.drawable.bg_tab_unselected
            )
            binding.tabOrder.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unselected_tab_text_color)
            )
        }
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
                            binding.tvInvoice.setText(
                                "${getString(R.string.invoice)}: ${data.invoice_number}"
                            )
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
                                requireContext(), it.data.message, false
                            )
                        }
                    }

                    is Order2Status.SaveOrder -> {
                        dialog.dismiss()
                        if (it.saveOrderRes.status == 200) {
                            if (isSend == false) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.saveOrderRes.message,
                                    isSuccess = true,
                                    seconds = 2,
                                )
                            } else if (isSend == true) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.saveOrderRes.message,
                                    isSuccess = true,
                                    seconds = 2,
                                    onAutoDismiss = {
                                        findNavController().navigate(
                                            R.id.toHome,
                                            null,
                                            androidx.navigation.NavOptions.Builder().setPopUpTo(
                                                findNavController().graph.startDestinationId,
                                                true
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
                                requireContext(), it.saveOrderRes.message, false
                            )
                        }
                    }

                    is Order2Status.EditOrder -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = true,
                                seconds = 2,
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
                                requireContext(), it.data.message, false
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
                                requireContext(), it.data.message, false
                            )
                        }
                    }

                    is Order2Status.GetItemDetails -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            getItemsResponse = it.data.data.toMutableList()
//                            prefillOrderSelections()
                            it.data.data.forEach { detail ->
                                val product = allProducts.firstOrNull {
                                    it.ITEM_CODE == detail.ITEM_CODE
                                }
                                product?.orderedQuantity =
                                    detail.QUANTITY.toIntOrNull() ?: 0
                            }
                            pendingItem?.let { item ->
                                val selections = currentSelections()
                                if (pendingQty > 0) {
                                    selections[item.ITEM_CODE] = pendingQty
                                } else {
                                    selections.remove(item.ITEM_CODE)
                                }
                                binding.tabSelected.text =
                                    "${getString(R.string.selected)} (${selections.size})"
                                calculateTotals()
                                if (isSelectedTab) {
                                    updateScreen()
                                } else {
                                    binding.rvProducts.adapter?.notifyDataSetChanged()
                                }
                            }
                            pendingItem = null
                            pendingQty = 0
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
                                requireContext(), it.data.message, false
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
//                            getData()
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
        val adapter = ProductAdapter(
            list, currentSelections(), object : ProductAdapter.OnItemActionListener {

                override fun onItemClicked(
                    item: Product
                ) {
                }

                override fun onQuantityChanged(
                    item: Product,
                    qty: Int
                ) {

                    Log.d("WHATclicked", item.clicked.toString())
                    pendingItem = item
                    pendingQty = qty
                    if (item.clicked == true) {
                        pendingItem?.let { item ->
                            val selections = currentSelections()
                            if (pendingQty > 0) {
                                selections[item.ITEM_CODE] = pendingQty
                            } else {
                                selections.remove(item.ITEM_CODE)
                            }
                            binding.tabSelected.text =
                                "${getString(R.string.selected)} (${selections.size})"
                            calculateTotals()
                            if (isSelectedTab) {
                                updateScreen()
                            } else {
                                binding.rvProducts.adapter?.notifyDataSetChanged()
                            }
                        }
                        pendingItem = null
                        pendingQty = 0
                    } else if (item.clicked == false) {
                        item.clicked = true
                        getItemDetails(
                            item.ITEM_CODE,
                            priceListId,
                            storeId
                        )
                    }
                }
            })

        binding.rvProducts.layoutManager = LinearLayoutManager(
            requireContext()
        )

        binding.rvProducts.adapter = adapter
        binding.rvProducts.itemAnimator = DefaultItemAnimator()
    }

    private fun filterProducts(
        keyword: String
    ) {

        val baseList =
            if (isSelectedTab) {
                allProducts.filter {
                    (currentSelections()[it.ITEM_CODE] ?: 0) > 0
                }
            } else {
                allProducts
            }

        displayedProducts =
            if (keyword.isBlank()) {
                baseList.toMutableList()
            } else {
                baseList.filter {
                    it.PRODUCT_NAME.contains(
                        keyword, true
                    ) ||
                            it.ITEM_CODE.contains(
                                keyword, true
                            )

                }.toMutableList()
            }

        setRecycler(displayedProducts)

        binding.llZeroState.visibility = if (displayedProducts.isEmpty()) View.VISIBLE
        else View.GONE
    }

    private fun calculateTotals() {
        val selections = currentSelections()
        var totalQty = 0
        var beforeTax = 0.0
        allProducts.forEach { product ->
            val qty = selections[product.ITEM_CODE] ?: 0
            totalQty += qty
            beforeTax += qty * product.ITEM_PRICE.toDouble()
        }

        val tax = beforeTax * 0.14
        val afterTax = beforeTax + tax
        binding.tvSelectedCount.text =
            "${currentSelections().size} ${requireContext().getString(R.string.product)} (${totalQty} ${
                requireContext().getString(R.string.piece)
            })"
        binding.tvBeforeTax.text =
            "${"%.2f".format(beforeTax)} ${requireContext().getString(R.string.currency)}"
        binding.tvTax.text =
            "${"%.2f".format(tax)} ${requireContext().getString(R.string.currency)}"
        binding.tvAfterTax.text =
            "${"%.2f".format(afterTax)} ${requireContext().getString(R.string.currency)}"
    }

    private fun updateScreen() {
        val selections = currentSelections()
        val displayedList = if (isSelectedTab) {
            allProducts.filter {
                selections.containsKey(
                    it.ITEM_CODE
                )
            }.toMutableList()
        } else {
            allProducts
        }
        binding.tabSelected.setText(
            "${requireContext().getString(R.string.selected)} (${currentSelections().size})"
        )
        if (displayedList.isEmpty()) {
            binding.llZeroState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.llZeroState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
        }
        filterProducts(
            binding.etSearch.text.toString()
        )
        binding.layoutSummary.visibility = if (isSelectedTab) View.VISIBLE
        else View.GONE
        calculateTotals()
    }

    private fun currentSelections(): MutableMap<String, Int> {
        return if (isOrderTab) orderSelections
        else returnSelections
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

    private fun String.normalizeName(): String {
        return this.replace("'", "").replace("\"", "").replace("،", "").replace(".", "")
            .replace("-", "").trim().lowercase()
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

    private fun getItemDetails(itemId: String, priceList: String, storeId: String) {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.GetItemDetails(
                    itemId, priceList, storeId
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_invoice, container, false
        )
        return binding.root
    }

}