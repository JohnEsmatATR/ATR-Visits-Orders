package com.akhnaton.foodvisits.ui.home.returns

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getItemDetailsReturn.PRICES
import com.akhnaton.foodvisits.data.model.getPriceLists.DamagePriceId
import com.akhnaton.foodvisits.data.model.getPriceLists.PriceId
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.model.saveReturn.SaveReturnItemReq
import com.akhnaton.foodvisits.data.model.saveReturn.SaveReturnReq
import com.akhnaton.foodvisits.data.model.saveReturn.asMessage
import com.akhnaton.foodvisits.data.model.startReturnData.Product
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsIntent
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsStatus
import com.akhnaton.foodvisits.databinding.FragmentReturnsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.order.OrderSummaryBottomSheet
import com.akhnaton.foodvisits.ui.home.order.ProductReturnsBottomSheet
import com.akhnaton.foodvisits.ui.home.order.products.ReturnsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ReturnsFragment : Fragment() {

    companion object {
        private const val TAG = "ReturnsFragment"
    }

    private val viewModel: ReturnsViewModel by viewModels()
    private var _binding: FragmentReturnsBinding? = null
    private var fromFragment: String = ""
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog

    private lateinit var backPressedCallback: OnBackPressedCallback

    lateinit var customerPartySiteId: String
    lateinit var saleType: String
    lateinit var orderId: String
    lateinit var storeId: String
    lateinit var selectedDamagePriceList: DamagePriceId
    lateinit var selectedPriceList: PriceId
    lateinit var returnId: String
    private var paymentId: Int = 0
    private var allProducts = mutableListOf<Product>()
    private lateinit var orderAdapter: ReturnsAdapter
    private var selectedProduct: Product? = null
    private var selectedProducts = mutableListOf<Product>()

    var totalQty = 0
    var beforeTax = 0.0
    var tax = 0.0
    var afterTax = 0.0
    var total = 0.0
    private var isSavedBefore: Boolean? = false
    private var isSend: Boolean? = false
    private var selectedPrice: PRICES? = null
    private var isRefreshTokenCalled = false
    private var hasRetriedAfterRefresh = false
    private val pendingRetries = mutableListOf<() -> Unit>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboardInsets()

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()
        orderId = arguments?.getString("orderId").toString()
        storeId = arguments?.getString("storeId").toString()
        fromFragment = arguments?.getString("fragment").orEmpty()

        selectedPriceList = Gson().fromJson(
            arguments?.getString("priceList"),
            PriceId::class.java
        )
        selectedDamagePriceList = Gson().fromJson(
            arguments?.getString("damagePriceList"),
            DamagePriceId::class.java
        )
        val returnData = Gson().fromJson(
            arguments?.getString("returnData"),
            com.akhnaton.foodvisits.data.model.startReturnData.Data::class.java
        )
        val selectedOptions = Gson().fromJson(
            arguments?.getString("selectedOptions"),
            Array<SelectedOption>::class.java
        )

        returnId = returnData.return_id
        allProducts = returnData.products.toMutableList()

//        if (isEdit == true) {
//            prefillOrderSelections()
//        }

        paymentId = selectedOptions
            ?.firstOrNull { it.key?.contains(other = "payment", ignoreCase = true) == true }
            ?.id
            ?.toInt() ?: 0

        val ltrNumber = "\u200E${returnId}\u200E"
        binding.tvInvoice.text = "${getString(R.string._return)}: $ltrNumber"

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        //MainActivity.binding.navView2.visibility = View.GONE

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

        orderAdapter = ReturnsAdapter(
            selectedProducts,
            object : ReturnsAdapter.OnItemActionListener {

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

        binding.btnAddOrder.setOnClickListener {
            Log.d("WHATisSavedBefore", isSavedBefore.toString())
            if (isSavedBefore == false) {
                DialogUtils.showResultDialog(
                    requireContext(), "يجب اضافة طلبية أولا", false,
                    showOkButton = true,
                )
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("customerPartySiteId", customerPartySiteId)
                putString("saleType", saleType)
                putString("orderId", orderId)
                putString("storeId", storeId)
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
//                beforeTax = beforeTax.toString(),
//                tax = tax.toString(),
//                afterTax = afterTax.toString(),
                total = total.toString(),
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
            ProductReturnsBottomSheet(
                allProducts,
                object : ProductReturnsBottomSheet.OnProductSelected {
                    override fun onSelected(product: Product) {
                        binding.etQty.requestFocus()
                        selectedProduct = product
                        binding.etSearch.setText(product.DESCRIPTION)
                        getItemDetails(
                            product.INVENTORY_ITEM_ID,
                            selectedPriceList.PRICE_LIST_ID,
                            storeId
                        )
                        binding.layoutAdd.visibility = View.VISIBLE
                    }
                }
            ).show(parentFragmentManager, "products")
        }

        binding.etSearch.setOnClickListener {
            ProductReturnsBottomSheet(
                allProducts,
                object : ProductReturnsBottomSheet.OnProductSelected {
                    override fun onSelected(product: Product) {
                        binding.etQty.requestFocus()
                        selectedProduct = product
                        binding.etSearch.setText(product.DESCRIPTION)
                        getItemDetails(
                            product.INVENTORY_ITEM_ID,
                            selectedPriceList.PRICE_LIST_ID,
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
            Log.d("WHATexist", selectedProducts.size.toString())

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

        orderAdapter.notifyDataSetChanged()
        updateEmptyView()
        updateTotal()
        calculateTotals()

        fetchData()
    }

    private fun saveOrder(send: Boolean) {
        val items = selectedProducts.mapIndexed { index, product ->
            (index + 1).toString() to SaveReturnItemReq(
                inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),
                quantity = product.selectedQty,
                price = product.price.toDouble(),
                customerPrice = product.cust_price.toDouble()
            )
        }.toMap()
//        val items = if (isSavedBefore == false) {
//            selectedProducts.mapIndexed { index, product ->
//                (index + 1).toString() to SaveReturnItemReq(
//                    inventoryItemId = product.INVENTORY_ITEM_ID.toInt(),
//                    quantity = product.selectedQty,
//                    price = product.price.toInt(),
//                    customerPrice = product.cust_price.toInt()
//                )
//            }.toMap()
//        } else {
//            ProductMapper.mapItemsSummaryToRequest(
//                itemsSummaryList,
//                selectedProducts
//            )
//        }
        val request = SaveReturnReq(
            returnId = returnId,
            orderId = orderId,
            orderType = saleType,
            comment = "",
            partySiteId = customerPartySiteId,
            send = if (send) "1" else "0",
            priceListId = selectedPriceList.PRICE_LIST_ID.toInt(),
            paymentId = paymentId,
            items = items,
        )

        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.SaveReturn(request)
            )
        }
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
            if (it.selectedQty != null && it.price != null) {
                total += it.selectedQty * it.price.toDouble()
            }
//            Log.d("WHATsaveditems", it.SAVED_ITEMS.size.toString())
//            Log.d("WHATsaveditems", it.SAVED_ITEMS.toString())
//            it.SAVED_ITEMS.forEach {
//                total += it.TOTAL_VALUE.toDouble()
//                Log.d("WHATsaveditems", it.TOTAL_VALUE)
//                Log.d("WHATsaveditems", total.toString())
//            }
//            total += it.selectedQty * (it.SAVED_ITEMS[0].UNIT_PRICE.toDouble() ?: 0)
        }
        binding.tvProductsCount.text =
            "${selectedProducts.size} أصناف"
        binding.tvTotal.text =
            String.format("%.2f %s", total, getString(R.string.currency))
    }

    private fun calculateTotals() {
        totalQty = 0
        beforeTax = 0.0
//        tax = 0.0
        selectedProducts.forEach {
            totalQty += it.selectedQty
            beforeTax +=
                it.selectedQty * it.price.toDouble()
//            tax +=
//                it.selectedQty * it.TAX_AMOUNT.toDouble()
//            beforeTax += if (it.SAVED_ITEMS.size > 0 && it.SAVED_ITEMS != null) {
//                it.selectedQty * it.SAVED_ITEMS[0].UNIT_PRICE.toDouble()
//            } else {
//                0.0
//            }
//            tax += if (it.SAVED_ITEMS.size > 0 && it.SAVED_ITEMS != null) {
//                it.selectedQty * it.SAVED_ITEMS[0].TAX.toDouble()
//            } else {
//                0.0
//            }
            afterTax = beforeTax + tax
        }
//        tax = (beforeTax * .14).toInt()
    }

    private fun getItemDetails(itemId: String, priceList: String, storeId: String) {
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.GetItemDetails(
                    itemId, priceList, storeId
                )
            )
        }
    }

    private fun handle401(message: String, retry: () -> Unit) {
        if (hasRetriedAfterRefresh) {
            hasRetriedAfterRefresh = false
            pendingRetries.clear()
            showSessionExpired(message)
            return
        }
        pendingRetries.add(retry)
        sendRefreshToken()
    }

    private fun sendRefreshToken() {
        Log.d(TAG, "sendRefreshToken called, isRefreshTokenCalled=$isRefreshTokenCalled")
        if (isRefreshTokenCalled) return
        isRefreshTokenCalled = true
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
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

    private fun fetchData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect {
                    Log.d(TAG, "status: $it")
                    when (it) {
                        is ReturnsStatus.Idle -> {}
                        is ReturnsStatus.Loading -> dialog.show()

                        is ReturnsStatus.GetItemDetails -> {
                            dialog.dismiss()
                            if (it.data.status != 401) hasRetriedAfterRefresh = false
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.getItemDetailsReturn.Data::class.java
                                )
                                val product = allProducts.firstOrNull() { pro ->
                                    pro.INVENTORY_ITEM_ID == data.INVENTORY_ITEM_ID
                                }
                                product?.TOTAL_QUANTITY = data.QUANTITY.toInt()
                                binding.etQty.hint =
                                    "${product?.TOTAL_QUANTITY ?: 0}"
                                binding.rvPrices.adapter = PricesAdapter(
                                    data.PRICES.toMutableList(),
                                    object : PricesAdapter.OnPriceSelected {
                                        override fun onPriceSelected(price: PRICES) {
                                            selectedPrice = price
                                            product?.cust_price = selectedPrice?.CUST_PRICE!!
                                            product?.price = selectedPrice?.OPERAND!!
                                        }
                                    })
                                binding.rvPrices.apply {
                                    layoutManager = LinearLayoutManager(requireContext())
                                }
//                                binding.rvProducts.apply {
//                                    layoutManager = LinearLayoutManager(requireContext())
//                                    adapter = orderAdapter
//                                }
                            } else if (it.data.status == 401) {
                                val product = selectedProduct
                                handle401(it.data.message) {
                                    product?.let { p ->
                                        getItemDetails(
                                            p.INVENTORY_ITEM_ID,
                                            selectedPriceList.PRICE_LIST_ID,
                                            storeId
                                        )
                                    }
                                }
                            } else {
                                DialogUtils.showResultDialog(
                                    requireContext(), it.data.message, false,
                                    showOkButton = true,
                                )
                            }
                        }

                        is ReturnsStatus.SaveReturn -> {
                            dialog.dismiss()
                            val message = it.data.message.asMessage()
                            Log.d(TAG, "SaveReturn status=${it.data.status} message=$message")
                            if (it.data.status != 401) hasRetriedAfterRefresh = false

                            when (it.data.status) {
                                200 -> {
                                    if (isSend == true) {
//                                        MainActivity.binding.navView2.visibility = View.VISIBLE
                                        DialogUtils.showResultDialog(
                                            context = requireContext(),
                                            message = message,
                                            isSuccess = true,
                                            showOkButton = true,
                                            onOk = {
                                                findNavController().navigate(
                                                    R.id.toHome,
                                                    null,
                                                    androidx.navigation.NavOptions.Builder()
                                                        .setPopUpTo(
                                                            findNavController().graph.startDestinationId,
                                                            true
                                                        ).build()
                                                )
                                            })
                                    } else {
//                                itemsSummaryList = data.items_summary.toMutableList()
//                                Log.d("WHATitemsSummaryList", itemsSummaryList.toString())
//                                setRecycler(itemsSummaryList)
                                        isSavedBefore = true
                                        DialogUtils.showResultDialog(
                                            context = requireContext(),
                                            message = message,
                                            isSuccess = true,
                                            showOkButton = true,
                                            onOk = {})
                                    }
                                }

                                400 -> {
                                    val percentage = runCatching {
                                        Gson().fromJson(
                                            it.data.data,
                                            com.akhnaton.foodvisits.data.model.saveReturn.Data::class.java
                                        ).return_percentage
                                    }.getOrNull()

                                    DialogUtils.showResultDialog(
                                        requireContext(),
                                        message,
                                        description = percentage?.let { p -> "نسبة المرتجع حاليا $p%" }
                                            ?: "",
                                        isSuccess = false,
                                        showOkButton = true,
                                    )
                                }

                                401 -> handle401(message) { saveOrder(isSend == true) }

                                else -> DialogUtils.showResultDialog(
                                    requireContext(), message, false, showOkButton = true
                                )
                            }
                        }

                        is ReturnsStatus.RefreshToken -> {
                            Log.d(TAG, "RefreshToken status=${it.data.status} message=${it.data.message}")
                            dialog.hide()
                            isRefreshTokenCalled = false
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
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

                        is ReturnsStatus.Error -> {
                            isRefreshTokenCalled = false
                            hasRetriedAfterRefresh = false
                            pendingRetries.clear()
                            Log.d(TAG, "fetchData: ${it.error}")
                            dialog.hide()
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.error.orEmpty(),
                                isSuccess = false,
                                showOkButton = true,
                            )
                        }

                        else -> {}
                    }
                }
                viewModel.consumeStatus()
            }
        }
    }
    private fun goBackToVisits() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_returns, container, false
        )
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}