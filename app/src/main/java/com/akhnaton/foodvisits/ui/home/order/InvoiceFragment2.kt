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
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.model.saveOrder.ItemsSummary
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderItemReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.databinding.FragmentInvoice2Binding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.mappers.ProductMapper
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

    private var selectedProducts = mutableListOf<Product>()

    lateinit var orderId: String
    var paymentId: String? = ""
    var warehouseId: String? = ""
    var saleTypeId: String? = ""
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

    var totalQty = 0
    var beforeTax = 0.0

    var tax = 0.0
    var afterTax = 0.0
    var total = 0.0
    private var isSavedBefore: Boolean? = false
    private var itemsSummaryList = mutableListOf<ItemsSummary>()

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

                    updateTotal()
                    updateEmptyView()
                    calculateTotals()
                }

                override fun onDeleteClicked(item: Product) {
                    val index = selectedProducts.indexOf(item)
                    if (index != -1) {
                        selectedProducts.removeAt(index)
                        orderAdapter.notifyItemRemoved(index)
                    }
                    updateTotal()
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
//            if (isSavedBefore == false) {
//                binding.btnAddReturn.isClickable = false
//                binding.btnAddReturn.isEnabled = false
//            } else if (isSavedBefore == true) {
//                binding.btnAddReturn.isClickable = true
//                binding.btnAddReturn.isEnabled = true
//            }

            val bundle = Bundle().apply {
                putString("customerPartySiteId", customerPartySiteId)
                putString("saleType", saleType)
            }

            findNavController().navigate(
                R.id.toReturns,
                bundle,
                androidx.navigation.NavOptions.Builder().setPopUpTo(
                    findNavController().graph.startDestinationId, true
                ).build()
            )
        }

        binding.cardBottom.setOnClickListener {
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
                beforeTax = beforeTax.toString(),
                tax = tax.toString(),
                afterTax = afterTax.toString(),
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
            if (existing == null) {
                product.selectedQty = qty
                selectedProducts.add(product)
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
            updateTotal()
            updateEmptyView()
            calculateTotals()
//            updateTotal()
            binding.etSearch.text = ""
            binding.etQty.text?.clear()
            binding.layoutAdd.visibility = View.GONE
            selectedProduct = null
        }
        getStartOrderData()
        fetchData()
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

    private fun updateTotal() {
        total = 0.0
        selectedProducts.forEach {
            total += it.selectedQty * it.ITEM_PRICE.toDouble()
        }
        binding.tvProductsCount.text =
            "${selectedProducts.size} أصناف"
        binding.tvTotal.text =
            String.format("%.2f %s", total, getString(R.string.currency))
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
                it.selectedQty = preview.QUANTITY.toInt()
                selectedProducts.add(it)
            }

            orderAdapter.notifyDataSetChanged()
            updateTotal()
            updateEmptyView()
            calculateTotals()
        }
    }

    private fun calculateTotals() {
        totalQty = 0
        beforeTax = 0.0
        tax = 0.0
        selectedProducts.forEach {
            totalQty += it.selectedQty
            beforeTax +=
                it.selectedQty * it.ITEM_PRICE.toDouble()
            tax +=
                it.selectedQty * it.TAX_AMOUNT.toDouble()
            afterTax = beforeTax + tax
        }
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
        Log.d("WHATmap", selectedProducts.toString())
        val items = if (isSavedBefore == false) {
            selectedProducts.mapIndexed { index, product ->
                (index + 1).toString() to SaveOrderItemReq(
                    inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),
                    quantity = product.selectedQty
                )
            }.toMap()
        } else {
            ProductMapper.mapItemsSummaryToRequest(
                itemsSummaryList,
                selectedProducts
            )
        }

        val request = SaveOrderReq(
            orderId = orderId,
            partySiteId = customerPartySiteId,
            orderType = saleType,
            deviceType = "android",
            send = if (send) "1" else "0",
            warehouseType = warehouseId ?: "",
            paymentId = paymentId?.toIntOrNull() ?: 106014,
            items = items
        )

        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.SaveOrder(request)
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
                    updateTotal()
                    updateEmptyView()
                    calculateTotals()
                }

                override fun onDeleteClicked(item: Product) {
                    val index = selectedProducts.indexOf(item)
                    if (index != -1) {
                        selectedProducts.removeAt(index)
                        orderAdapter.notifyItemRemoved(index)
                    }
                    updateTotal()
                    updateEmptyView()
                    calculateTotals()
                }
            })
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
        updateEmptyView()
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

                            orderAdapter.notifyDataSetChanged()
                            updateEmptyView()
                            updateTotal()
                            calculateTotals()
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
                                isSavedBefore = true
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.saveOrderRes.message.firstOrNull().orEmpty(),
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                    })
                                itemsSummaryList = data.items_summary.toMutableList()
                                Log.d("WHATitemsSummaryList", itemsSummaryList.toString())
                                setRecycler(itemsSummaryList)
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
                            orderAdapter.notifyDataSetChanged()
                            updateTotal()
                            updateEmptyView()
                            calculateTotals()
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
                            binding.etQty.hint =
                                "${product?.TOTAL_QUANTITY ?: 0}"
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