package com.akhnaton.foodvisits.ui.home.returns

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getItemDetailsReturn.PRICES
import com.akhnaton.foodvisits.data.model.getPriceLists.DamagePriceId
import com.akhnaton.foodvisits.data.model.getPriceLists.PriceId
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderItemReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.model.saveReturn.SaveReturnItemReq
import com.akhnaton.foodvisits.data.model.saveReturn.SaveReturnReq
import com.akhnaton.foodvisits.data.model.startReturnData.Product
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsIntent
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsStatus
import com.akhnaton.foodvisits.databinding.FragmentInvoice2Binding
import com.akhnaton.foodvisits.databinding.FragmentReturnsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.mappers.ProductMapper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.InvoiceFragment2
import com.akhnaton.foodvisits.ui.home.order.Order2ViewModel
import com.akhnaton.foodvisits.ui.home.order.OrderSummaryBottomSheet
import com.akhnaton.foodvisits.ui.home.order.ProductBottomSheet
import com.akhnaton.foodvisits.ui.home.order.ProductReturnsBottomSheet
import com.akhnaton.foodvisits.ui.home.order.products.ProductAdapter
import com.akhnaton.foodvisits.ui.home.order.products.ReturnsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.getValue

class ReturnsFragment : Fragment() {

    companion object {
        private const val TAG = "ReturnsFragment"
    }

    private val viewModel: ReturnsViewModel by viewModels()
    private lateinit var binding: FragmentReturnsBinding
    private lateinit var dialog: AlertDialog

    private lateinit var backPressedCallback: OnBackPressedCallback

    lateinit var customerPartySiteId: String
    lateinit var saleType: String
    lateinit var orderId: String
    lateinit var storeId: String
    lateinit var selectedDamagePriceList: DamagePriceId
    lateinit var selectedPriceList: PriceId
    lateinit var returnId: String
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()
        orderId = arguments?.getString("orderId").toString()
        storeId = arguments?.getString("storeId").toString()

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
                            selectedDamagePriceList.PRICE_LIST_ID,
                            storeId
                        )
                        binding.layoutAdd.visibility = View.VISIBLE
                    }
                }
            ).show(parentFragmentManager, "products")
        }

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
        updateEmptyView()

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
                    findNavController().popBackStack(
                        R.id.visitPhoneFragment, false
                    )
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
                    Log.d("WHATexist", it.ITEM_CODE)
                    Log.d("WHATexist", product.ITEM_CODE)

                    it.ITEM_CODE == product.ITEM_CODE
                }
            if (existing == null) {
                product.selectedQty = qty
                selectedProducts.add(product)
                orderAdapter.notifyItemInserted(selectedProducts.lastIndex)
                Log.d("WHATexist", selectedProducts.size.toString())
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

        getPriceLists()
        fetchData()

    }

    private fun saveOrder(send: Boolean) {
        Log.d("WHATmap", selectedProducts.toString())
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
        Log.d("WHATsaveditems", selectedProducts.toString())
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

    private fun getPriceLists() {
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.GetPriceLists(
                    customerPartySiteId, saleType
                )
            )
        }
    }

    private fun startReturnData() {
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.StartReturnData(
                    orderId, selectedDamagePriceList.PRICE_LIST_ID
                )
            )
        }
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

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is ReturnsStatus.Idle -> {}
                    is ReturnsStatus.Loading -> dialog.show()

                    is ReturnsStatus.GetPriceLists -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            setRecycler(it.data.data.products.toMutableList())
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.getPriceLists.Data::class.java
                            )
                            selectedPriceList = data.price_list_id[0]
                            selectedDamagePriceList = data.damage_price_list_id[0]
                            startReturnData()
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.returnsIntent.send(
                                    ReturnsIntent.RefreshToken(
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

                    is ReturnsStatus.StartReturnData -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            setRecycler(it.data.data.products.toMutableList())
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.startReturnData.Data::class.java
                            )
                            returnId = data.return_id
                            binding.tvInvoice.text = returnId

                            val ltrNumber = "\u200E${returnId}\u200E"
                            binding.tvInvoice.text = "${getString(R.string._return)}: $ltrNumber"

                            allProducts = data.products.toMutableList()

//                            if (isEdit == true) {
//                                prefillOrderSelections()
//                            }

                            orderAdapter.notifyDataSetChanged()
                            updateEmptyView()
                            updateTotal()
                            calculateTotals()

                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.returnsIntent.send(
                                    ReturnsIntent.RefreshToken(
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

                    is ReturnsStatus.GetItemDetails -> {
                        dialog.dismiss()
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
//                            binding.rvProducts.apply {
//                                layoutManager = LinearLayoutManager(requireContext())
//                                adapter = orderAdapter
//                            }
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.returnsIntent.send(
                                    ReturnsIntent.RefreshToken(
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

                    is ReturnsStatus.SaveReturn -> {
                        dialog.dismiss()
                        val data = Gson().fromJson(
                            it.data.data,
                            com.akhnaton.foodvisits.data.model.saveReturn.Data::class.java
                        )
                        if (it.data.status == 200) {
                            if (isSend == false) {
                                isSavedBefore = true
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.data.message,
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                    })
//                                itemsSummaryList = data.items_summary.toMutableList()
//                                Log.d("WHATitemsSummaryList", itemsSummaryList.toString())
//                                setRecycler(itemsSummaryList)
                            } else if (isSend == true) {
                                MainActivity.binding.navView2.visibility = View.VISIBLE
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.data.message,
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

                        } else if (it.data.status == 400) {
                            DialogUtils.showResultDialog(
                                requireContext(),
                                it.data.message,
                                description = "نسبة المرتجع حاليا ${data.return_percentage}%",
                                isSuccess = false,
                                showOkButton = true,
                            )
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.returnsIntent.send(
                                    ReturnsIntent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(),
                                it.data.message,
                                false,
                                showOkButton = true,
                            )
                        }
                    }

                    is ReturnsStatus.RefreshToken -> {
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

                    is ReturnsStatus.Error -> {
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_returns, container, false
        )
        return binding.root
    }
}