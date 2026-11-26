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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.model.getStartOrderData.Product
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.model.saveOrder.ItemsSummary
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderItemReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.databinding.FragmentInvoice2Binding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.NumberUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.mappers.ProductMapper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.products.ProductAdapter
import com.google.gson.Gson
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
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
    var visitId: String = ""
    lateinit var selectedOptionsJson: String
    private var allProducts = mutableListOf<Product>()

    private var selectedProducts = mutableListOf<Product>()
    private var isRefreshTokenCalled = false
    private var hasRetriedAfterRefresh = false
    private val pendingRetries = mutableListOf<() -> Unit>()

    lateinit var orderId: String
    var paymentId: String? = ""
    var warehouseId: String? = ""
    var saleTypeId: String? = ""
    var bonus: String? = ""
    private var isEdit: Boolean? = false
    private var fromFragment: String = ""
    lateinit var OrigSysDocumentRef: String
    lateinit var orderJson: String
    private var getItemsResponse = mutableListOf<com.akhnaton.foodvisits.data.model.getItems.Data>()
    lateinit var orderData: com.akhnaton.foodvisits.data.model.getList.Data
    private var isSend: Boolean? = false
    lateinit var priceListId: String
    lateinit var storeId: String

    private lateinit var backPressedCallback: OnBackPressedCallback
    private var pendingInitialLoads = 0
    private var selectedProduct: Product? = null

    private lateinit var orderAdapter: ProductAdapter

    var totalQty = 0
    var beforeTax = 0.0
    var tax = 0.0
    var afterTax = 0.0
    var total = 0.0
    var totalFromApi = 0.0
    private var isSavedBefore: Boolean? = false
    private var restoreOrder = false
    private var itemsSummaryList = mutableListOf<ItemsSummary>()

    private lateinit var selectedOptionsArray: JSONArray
    private var orderSelects = mutableMapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromFragment = arguments?.getString("fragment").orEmpty()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setupKeyboardInsets()

        isEdit = arguments?.getBoolean("isEdit", false)
        visitId = arguments?.getString("visitId").orEmpty()
        restoreOrder = arguments?.getBoolean("restoreOrder", false) == true &&
                OrderStateHolder.orderId != null
        Log.d(
            "WHATrestore",
            "restoreOrder=$restoreOrder isEdit=$isEdit holderOrderId=${OrderStateHolder.orderId} argRestore=${
                arguments?.getBoolean(
                    "restoreOrder",
                    false
                )
            }"
        )
        if (restoreOrder) {
            orderId = OrderStateHolder.orderId!!
            customerPartySiteId = OrderStateHolder.customerPartySiteId!!
            customerCode = OrderStateHolder.customerCode.orEmpty()
            saleType = OrderStateHolder.saleType!!
            storeId = OrderStateHolder.storeId.orEmpty()
            priceListId = OrderStateHolder.priceListId.orEmpty()
            visitId = OrderStateHolder.visitId
            isSavedBefore = OrderStateHolder.isSavedBefore
            orderSelects = OrderStateHolder.orderSelects.toMutableMap()
            selectedProducts.clear()
            selectedProducts.addAll(OrderStateHolder.selectedProducts)
            customerName = ""
            siteAddress = ""
            getStartOrderData()
        } else if (!isEdit!!) {
            OrderStateHolder.clear()
            customerName = arguments?.getString("customerName").toString()
            customerCode = arguments?.getString("customerCode").toString()
            siteAddress = arguments?.getString("siteAddress").toString()
            customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
            saleType = arguments?.getString("saleType").toString()

            selectedOptionsJson = arguments?.getString("selectedOptions") ?: "[]"

            getStartOrderData()


//            selectedOptionsJson = arguments?.getString("selectedOptions").toString()
//
//            val type = object : TypeToken<List<SelectedOption>>() {}.type
//            val selectedOptions: List<SelectedOption> = Gson().fromJson(
//                selectedOptionsJson, type
//            )
//            paymentId = selectedOptions.firstOrNull {
//                it.key == "payment_terms"
//            }?.id ?: "106014"
//            warehouseId = selectedOptions.firstOrNull {
//                it.key == "warehouse_type"
//            }?.id
//            saleTypeId = selectedOptions.firstOrNull {
//                it.key == "sale_type"
//            }?.id
//            bonus = selectedOptions.firstOrNull {
//                it.key == "bonus"
//            }?.id

            selectedOptionsJson = arguments?.getString("selectedOptions") ?: "[]"


            // selectedOptionsArray = JSONArray(selectedOptionsJson)

//             Keep every option, including options with unknown keys.
//            val selectedOptions = (0 until selectedOptionsArray.length()).map { index ->
//                selectedOptionsArray.getJSONObject(index)
//            }
//            // Create a dynamic map: option key -> option ID.
//            val optionIds = selectedOptions.associate { option ->
//                option.optString("key") to option.optString("id")
//            }

            // Existing fields, if you still need them:
//            paymentId = optionIds["payment_terms"] ?: "106014"
//            warehouseId = optionIds["warehouse_type"]
//            saleTypeId = optionIds["sale_type"]
//            bonus = optionIds["bonus"]


            val listsJson = arguments?.getString("selectLists")
            if (!listsJson.isNullOrBlank()) {
                val type = object : TypeToken<List<SelectLists>>() {}.type
                val lists: List<SelectLists> = Gson().fromJson(listsJson, type)
                lists.forEach { list ->
                    val id = list.selectedId
                    if (!id.isNullOrBlank()) orderSelects[list.select] = id
                }
            }

            Log.d("WHATinvoice", paymentId.toString())
            Log.d("WHATinvoice", warehouseId.toString())
            Log.d("WHATinvoice", saleTypeId.toString())

        } else {
            OrigSysDocumentRef = arguments?.getString("OrigSysDocumentRef").toString()
            orderId = arguments?.getString("OrigSysDocumentRef").toString()
            orderJson = arguments?.getString("orderJson").toString()
            orderData = Gson().fromJson(
                orderJson, com.akhnaton.foodvisits.data.model.getList.Data::class.java
            )
            paymentId = orderData.PAYMENT_TERM_ID

//            selectedOptionsArray = loadOptionsLocally(OrigSysDocumentRef)
//                ?: JSONArray().put(
//                    org.json.JSONObject()
//                        .put("key", "payment_id")
//                        .put("id", orderData.PAYMENT_TERM_ID)
//                )
//            paymentId = "106014"

            customerCode = orderData.CUSTOMER_CODE
            customerPartySiteId = orderData.PARTY_SITE_ID
            saleType = orderData.ORDER_TYPE
            pendingInitialLoads = 2

            getStartOrderData()
            Log.d("WHAT", orderJson.toString())
            Log.d("WHAT", orderData.toString())
        }

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        //MainActivity.binding.navView2.visibility = View.GONE

        if (SharedPreferencesHelper.getInstance().isAllowedToMakeReturn())
            binding.btnAddReturn.visibility = View.VISIBLE
        else
            binding.btnAddReturn.visibility = View.GONE
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "هل انت متأكد انك تريد الرجوع ؟",
                    isSuccess = true,
                    showYesNoButtons = true,
                    onYes = {
                        goBackToVisits()
                    })
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backPressedCallback
        )

        orderAdapter = ProductAdapter(
            selectedProducts,
            object : ProductAdapter.OnItemActionListener {

                override fun onItemClicked(
                    item: Product
                ) {
                }

                override fun onQuantityChanged(item: Product) {

                    if (!item.clicked) {
//                        item.clicked = true
//                                getItemDetails(
//                                    item.INVENTORY_ITEM_ID,
//                                    priceListId,
//                                    storeId
//                                )
                    }
                    selectedProducts.firstOrNull { product ->
                        product.ITEM_CODE == item.ITEM_CODE
                    }?.let { product ->
                        val updatedProduct = product.copy(
                            selectedQty = item.selectedQty
                        )
                        val index = selectedProducts.indexOf(product)
                        selectedProducts[index] = updatedProduct
                    }
                    updateTotal(false)
                    updateEmptyView()
                    calculateTotals()
                }

                override fun onDeleteClicked(item: Product) {
                    val index = selectedProducts.indexOf(item)
                    if (index != -1) {
                        selectedProducts.removeAt(index)
                        orderAdapter.notifyItemRemoved(index)
                    }
                    updateTotal(false)
                    updateEmptyView()
                    calculateTotals()
                }
            })

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
        updateEmptyView()

        binding.btnAddReturn.setOnClickListener {
            Log.d("WHATisSavedBefore", isSavedBefore.toString())
            if (isSavedBefore == false && !isEdit!!) {
                DialogUtils.showResultDialog(
                    requireContext(), "يجب اضافة طلبية أولا", false,
                    showOkButton = true,
                )
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("fragment", fromFragment)
                putString("customerPartySiteId", customerPartySiteId)
                putString("saleType", saleType)
                putString("orderId", orderId)
                putString("storeId", storeId)
                putString("customerCode", customerCode)
            }
            persistOrderState()

            findNavController().navigate(
                R.id.toReturnOptions,
                bundle,
                androidx.navigation.NavOptions.Builder().setPopUpTo(
                    findNavController().graph.startDestinationId, true
                ).build()
            )
        }

        binding.cardBottom.setOnClickListener {
            if (selectedProducts.isEmpty()) {
                DialogUtils.showResultDialog(
                    requireContext(), "لا يوجد أصناف في الطلب", false,
                    showOkButton = true,
                )
                return@setOnClickListener
            }
            if (total == 0.0) {
                DialogUtils.showResultDialog(
                    requireContext(), "يجب تحديد الكمية لكل صنف", false,
                    showOkButton = true,
                )
                return@setOnClickListener
            }
            calculateTotals()
            OrderSummaryBottomSheet(
                selectedCount = selectedProducts.size.toString(),
                beforeTax = "${NumberUtils.money(beforeTax)} ${getString(R.string.currency)}",
                tax = "${NumberUtils.money(tax)} ${getString(R.string.currency)}",
                afterTax = "${NumberUtils.money(afterTax)} ${getString(R.string.currency)}",
                listener = object : OrderSummaryBottomSheet.Listener {

                    override fun onSave() {
                        isSend = false
                        saveOrder(false)
                    }

                    override fun onSend() {
                        isSend = true
                        saveOrder(true)
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
                    goBackToVisits()
                })
        }

        binding.searchLayout.setOnClickListener {
            ProductBottomSheet(
                allProducts,
                object : ProductBottomSheet.OnProductSelected {
                    override fun onSelected(product: Product) {
                        binding.etQty.requestFocus()
                        selectedProduct = product
                        binding.etSearch.setText(product.PRODUCT_NAME)
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
                        binding.etQty.requestFocus()
                        selectedProduct = product
                        binding.etSearch.setText(product.PRODUCT_NAME)
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
            if (binding.etQty.text.toString().isEmpty()) {
                DialogUtils.showResultDialog(
                    requireContext(), "يجب اضافة كمية للمنتج", false,
                    showOkButton = true,
                )
                return@setOnClickListener
            }
            val product = selectedProduct ?: return@setOnClickListener
            val qty =
                binding.etQty.text.toString().toIntOrNull() ?: 1
            val existing =
                selectedProducts.firstOrNull {
                    it.ITEM_CODE == product.ITEM_CODE
                }
            Log.d("WHATexist", existing.toString())
            Log.d("WHATexist", selectedProducts.size.toString())
            if (existing == null) {
                product.selectedQty = qty
                selectedProducts.add(product)
                Log.d("WHATexist", selectedProducts.size.toString())
                orderAdapter.notifyItemInserted(selectedProducts.lastIndex)
            } else {
                DialogUtils.showResultDialog(
                    requireContext(), "المنتج موجود بالفعل", false,
                    showOkButton = true,
                )
                return@setOnClickListener
//                existing.selectedQty += qty
//                orderAdapter.notifyItemChanged(
//                    selectedProducts.indexOf(existing)
//                )
            }

            updateTotal(false)
            updateEmptyView()
            calculateTotals()
//            updateTotal()
            binding.etSearch.text = ""
            binding.etQty.text?.clear()
            binding.layoutAdd.visibility = View.GONE
            selectedProduct = null
        }
        if (restoreOrder) {
            binding.tvInvoice.text = "${getString(R.string.invoice)}: \u200E${orderId}\u200E"
            refreshUi()
        }

        fetchData()
    }

    private fun persistOrderState() {
        OrderStateHolder.apply {
            this.orderId = this@InvoiceFragment2.orderId
            this.customerPartySiteId = this@InvoiceFragment2.customerPartySiteId
            this.customerCode = this@InvoiceFragment2.customerCode
            this.saleType = this@InvoiceFragment2.saleType
            this.storeId = this@InvoiceFragment2.storeId
            this.priceListId = this@InvoiceFragment2.priceListId
            this.visitId = this@InvoiceFragment2.visitId
            this.isSavedBefore = true
            this.selectedProducts = this@InvoiceFragment2.selectedProducts.toMutableList()
            this.orderSelects = this@InvoiceFragment2.orderSelects.toMutableMap()
        }
    }

    private fun finishInitialLoad() {
        if (pendingInitialLoads > 0) pendingInitialLoads--
        if (pendingInitialLoads == 0) dialog.dismiss()
    }

    private fun updateEmptyView() {
        if (selectedProducts.isEmpty()) {
            binding.llZeroState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.llZeroState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
        }
    }

    private fun updateTotal(isFromApi: Boolean) {
        Log.d("isFromAPi", isFromApi.toString())
        if (isFromApi) total = totalFromApi
        else {
            if (selectedProducts.isNotEmpty()) Log.d(
                "WHATselectedProducts",
                selectedProducts.last().toString()
            )
            total = 0.0
            selectedProducts.forEach {
                if (it.IS_BACK_ORDER == false || it.IS_BACK_ORDER == null)
                    total += it.selectedQty * (it.ITEM_PRICE.toDouble() + it.TAX_AMOUNT.toDouble())
            }
        }

        binding.tvProductsCount.text =
            "${selectedProducts.size} أصناف"

        binding.tvTotal.text =
            "${NumberUtils.money(total)} ${getString(R.string.currency)}"
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

    private fun refreshUi() {
        orderAdapter.notifyDataSetChanged()
        updateTotal(false)
        updateEmptyView()
        calculateTotals()
        Log.d("WHATcount", "products=${selectedProducts.size} adapter=${orderAdapter.itemCount}")
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
        selectedProducts.clear()
        getItemsResponse.forEach { preview ->
            Log.d(
                "MATCH",
                "Searching for: ${preview.ITEM_NAME}, " +
                        "qty=${preview.QUANTITY}, " +
                        "backOrder=${preview.IS_BACK_ORDER}"
            )
            val matchedProduct = allProducts.firstOrNull {
                it.ITEM_CODE
                    .trim()
                    .replace("'", "") ==
                        preview.ITEM_CODE
                            .trim()
                            .replace("'", "")
            }
            Log.d(
                "MATCH",
                "Found: ${matchedProduct?.PRODUCT_NAME}"
            )
            matchedProduct?.let { product ->
                val selectedProduct = product.copy(
                    selectedQty = preview.QUANTITY.toInt(),
                    IS_BACK_ORDER = preview.IS_BACK_ORDER,
                    CHECKED = true
                )
                Log.d(
                    "MATCH",
                    "Adding product: " +
                            "name=${selectedProduct.PRODUCT_NAME}, " +
                            "qty=${selectedProduct.selectedQty}, " +
                            "backOrder=${selectedProduct.IS_BACK_ORDER}"
                )
                selectedProducts.add(selectedProduct)
            }
        }
        Log.d(
            "MATCH",
            "selectedProducts size = ${selectedProducts.size}"
        )
        selectedProducts.forEachIndexed { index, product ->
            Log.d(
                "MATCH",
                "selectedProducts[$index] = " +
                        "${product.PRODUCT_NAME}, " +
                        "qty=${product.selectedQty}, " +
                        "backOrder=${product.IS_BACK_ORDER}"
            )
        }
        refreshUi()
    }

    private fun calculateTotals() {
        totalQty = 0
        beforeTax = 0.0
        tax = 0.0
        selectedProducts.forEach {
            Log.d(
                "WHATtax", "name=${it.PRODUCT_NAME} tax=${it.TAX_AMOUNT} "
            )
            if (it.IS_BACK_ORDER == false || it.IS_BACK_ORDER == null) {
                totalQty += it.selectedQty
                beforeTax +=
                    it.selectedQty * it.ITEM_PRICE.toDouble()
                tax +=
                    it.selectedQty * it.TAX_AMOUNT.toDouble()
                afterTax = beforeTax + tax
            }
        }
        Log.d("WHATtax", "TOTAL beforeTax=$beforeTax tax=$tax afterTax=$afterTax")
//        tax = (beforeTax * .14).toInt()
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

    private fun saveOrder(send: Boolean) {

//        val items = if (isSavedBefore == false) {
//            selectedProducts.mapIndexed { index, product ->
//                (index + 1).toString() to SaveOrderItemReq(
//                    inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),
//                    quantity = product.selectedQty
//                )
//            }.toMap()
//        } else {
//            ProductMapper.mapItemsSummaryToRequest(
//                itemsSummaryList,
//                selectedProducts
//            )
//        }
        Log.d("WHATmap", selectedProducts.toString())
        val items = selectedProducts.mapIndexed { index, product ->
            (index + 1).toString() to SaveOrderItemReq(
                inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),
                quantity = product.selectedQty
            )
        }.toMap()

        val request = SaveOrderReq(
            orderId = orderId,
            partySiteId = customerPartySiteId,
            orderType = saleType,
            deviceType = "android",
            send = if (send) "1" else "0",
//            warehouseType = warehouseId ?: "",
//            paymentId = paymentId?.toIntOrNull() ?: 106014,
            items = items,
            visitId = visitId
        )

        val gson = Gson()

        // Convert the fixed fields to JSON.
        val requestJson = gson.toJsonTree(request).asJsonObject

        // Add every dynamic option.
//        for (i in 0 until selectedOptionsArray.length()) {
//            val option = selectedOptionsArray.optJSONObject(i) ?: continue
//
//            val key = option.optString("key")
//            if (key.isBlank() || key == "null") continue
//
//            val value = option.opt("id") ?: continue
//
//            requestJson.add(key, JsonPrimitive(value.toString()))
//        }
//selects map =>
        orderSelects.forEach { (key, value) ->
            requestJson.add(key, JsonPrimitive(value))
        }
        Log.d("WHATrequest", requestJson.toString())
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.SaveOrder(requestJson)
            )
        }
    }

    private fun setRecycler(
        allSummaries: MutableList<ItemsSummary>
    ) {
        selectedProducts = ProductMapper.mapItemSummaryToProducts(
            allSummaries,
            allProducts
        ) as MutableList<Product>

        Log.d("WHATyouSay2", allSummaries.toString())
        Log.d("WHATyouSay2", selectedProducts.toString())

        orderAdapter = ProductAdapter(
            selectedProducts,
            object : ProductAdapter.OnItemActionListener {
                override fun onItemClicked(
                    item: Product
                ) {
                }

                override fun onQuantityChanged(item: Product) {
                    if (!item.clicked) {
//                        item.clicked = true
//                                getItemDetails(
//                                    item.INVENTORY_ITEM_ID,
//                                    priceListId,
//                                    storeId
//                                )
                    }

                    selectedProducts.firstOrNull { product ->
                        product.ITEM_CODE == item.ITEM_CODE
                    }?.let { product ->
                        val updatedProduct = product.copy(
                            selectedQty = item.selectedQty
                        )
                        val index = selectedProducts.indexOf(product)
                        selectedProducts[index] = updatedProduct
                    }

                    updateEmptyView()
                    updateTotal(false)
                    calculateTotals()

                }

                override fun onDeleteClicked(item: Product) {
                    val index = selectedProducts.indexOf(item)
                    if (index != -1) {
                        selectedProducts.removeAt(index)
                        orderAdapter.notifyItemRemoved(index)
                    }
                    updateTotal(true)
                    updateEmptyView()
                    calculateTotals()
                }
            })
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
        updateEmptyView()
        updateTotal(true)
        calculateTotals()
        Log.d("WHATcount", "products=${selectedProducts.size} adapter=${orderAdapter.itemCount}")
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Order2Status.Idle -> {}
                    is Order2Status.Loading -> dialog.show()

                    is Order2Status.GetStartOrderData -> {
                        finishInitialLoad()
                        if (it.data.status == 200) {
//                            setRecycler(it.data.data.products.toMutableList())
                            val data = Gson().fromJson(
                                it.data.data, Data::class.java
                            )
                            if (isEdit!!) getItems()
                            if (!isEdit!! && !restoreOrder) orderId = data.invoice_number
                            priceListId = data.price_list_id
                            storeId = data.store_id
                            val ltrNumber = "\u200E${orderId}\u200E"

                            binding.tvInvoice.text = "${getString(R.string.invoice)}: $ltrNumber"

                            allProducts = data.products.toMutableList()

                            if (isEdit == true) {
                                prefillOrderSelections()
                            }

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
                        val message =
                            if (it.saveOrderRes.message.size > 1) it.saveOrderRes.message.joinToString(
                                "\n"
                            ) { msg ->
                                "- $msg"
                            } else it.saveOrderRes.message.firstOrNull().orEmpty()
                        if (it.saveOrderRes.status == 200) {
                            val data = Gson().fromJson(
                                it.saveOrderRes.data,
                                com.akhnaton.foodvisits.data.model.saveOrder.Data::class.java
                            )
//                            saveOptionsLocally(orderId, selectedOptionsArray.toString())

                            if (isSend == false) {
                                isSavedBefore = true
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = message,
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                    })
                                totalFromApi = data.order_total
                                itemsSummaryList = data.items_summary.toMutableList()
                                Log.d("WHATitemsSummaryList", itemsSummaryList.toString())
                                setRecycler(itemsSummaryList)
                                persistOrderState()
                            } else if (isSend == true) {
                                OrderStateHolder.clear()
                                //MainActivity.binding.navView2.visibility = View.VISIBLE
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = message,
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
                                message,
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
                        finishInitialLoad()

                        if (it.data.status == 200) {
                            Log.d("WHATorderSelects", "reached ${it.data.order_summary}")
                            getItemsResponse = it.data.data.toMutableList()
                            Log.d("WHATgetItemsResponse", "reached ${getItemsResponse}")
                            if (visitId.isEmpty()) {
                                visitId = it.data.order_summary?.visit_id.orEmpty()
                            }
                            it.data.order_summary?.selects?.forEach { select ->
                                val key = select.key
                                val value = select.value
                                if (!key.isNullOrBlank() && value != null) {
                                    orderSelects[key] = value
                                }
                            }
                            Log.d("WHATorderSelects", orderSelects.toString())
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
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.getItemDetails.Data::class.java
                            )
                            val product = allProducts.firstOrNull() { pro ->
                                pro.INVENTORY_ITEM_ID == data.INVENTORY_ITEM_ID
                            }
                            product?.TOTAL_QUANTITY = data.QUANTITY.toInt()
                            binding.etQty.hint = "${product?.TOTAL_QUANTITY}" ?: ""
                            updateTotal(false)
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
                        isRefreshTokenCalled = false
                        if (it.data.status == 200) {
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                            )
                            SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                            SharedPreferencesHelper.getInstance()
                                .saveAllowedToMakeOrder(data.ALLOWED_TO_MAKE_ORDER)
                            SharedPreferencesHelper.getInstance()
                                .saveAllowedToMakeReturn(data.ALLOWED_TO_MAKE_RETURN)
                            SharedPreferencesHelper.getInstance()
                                .saveAllowedToMakeVisit(data.ALLOWED_TO_MAKE_VISIT)
                            hasRetriedAfterRefresh = true
                            val retries = pendingRetries.toList()
                            pendingRetries.clear()
                            retries.forEach { r -> r() }
                        } else {
                            pendingRetries.clear()
                            hasRetriedAfterRefresh = false
                            showSessionExpired(it.data.message)
                        }
                    }

                    is Order2Status.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()
                        DialogUtils.showResultDialog(
                            context = requireContext(),
                            message = it.error.toString(),
                            isSuccess = true,
                            showOkButton = true,
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun showSessionExpired(message: String) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message,
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(Intent(requireContext(), LoginActivity2::class.java))
                requireActivity().finishAffinity()
            })
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

    private fun goBackToVisits() {
        if (isEdit == true) {
            findNavController().popBackStack()
            return
        }
        val nav = findNavController()
        if (fromFragment == "Telephone") {
            if (!nav.popBackStack(R.id.visitPhoneFragment, false)) {
                nav.navigate(R.id.visitPhoneFragment)
            }
        } else {
            if (!nav.popBackStack(R.id.visitsFragment, false)) {
                nav.navigate(R.id.visitsFragment)
            }
        }
    }

    private fun saveOptionsLocally(orderRef: String, optionsJson: String) {
        requireContext()
            .getSharedPreferences("order_options", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(orderRef, optionsJson)
            .apply()
    }

    private fun loadOptionsLocally(orderRef: String): JSONArray? {
        val json = requireContext()
            .getSharedPreferences("order_options", android.content.Context.MODE_PRIVATE)
            .getString(orderRef, null) ?: return null
        return JSONArray(json)
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