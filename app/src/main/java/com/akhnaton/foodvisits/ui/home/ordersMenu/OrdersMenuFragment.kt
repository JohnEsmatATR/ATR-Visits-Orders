package com.akhnaton.foodvisits.ui.home.ordersMenu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getList.Data
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.databinding.FragmentOrdersMenuBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.products.OrderMenuAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.CustomerDataAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits2.Visits2ViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.getValue

class OrdersMenuFragment : Fragment(),
    OrderMenuAdapter.OrderActionListener {

    companion object {
        private const val TAG = "OrdersMenuFragment"
    }

    private val viewModel: Visits2ViewModel by viewModels()
    private lateinit var binding: FragmentOrdersMenuBinding
    private lateinit var dialog: AlertDialog

    private val ordersList = mutableListOf<Data>()
    private lateinit var adapter: OrderMenuAdapter
    private var currentPage = 1
    private val pageSize = 20
    private var isLoading = false
    private var isLastPage = false
    private var currentStatus = ""
    private var searchText = ""
    private var selectedOrderType = ""
    private var fromDate = ""
    private var toDate = ""
    private var orderTypesList: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        MainActivity.binding.navView2.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.GetSalesAndCustomerTypes
            )
        }

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> resetAndLoad("")
                        1 -> resetAndLoad("saved")
                        2 -> resetAndLoad("sent")
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab) {}
            }
        )

        binding.btnFilter.setOnClickListener {
            FilterOrdersBottomSheet(
                currentStatus = currentStatus,
                currentFromDate = fromDate,
                currentToDate = toDate,
                orderTypesList = orderTypesList,
                currentOrderType = selectedOrderType
            ) { filter ->
                currentStatus = filter.status
                fromDate = filter.fromDate
                toDate = filter.toDate
                selectedOrderType = filter.orderType
                resetAndLoad(currentStatus)
            }
                .show(
                    childFragmentManager,
                    "FILTER"
                )
        }

        binding.etSearch.afterTextChangedDelayed {
            searchText =
                it.toString()
            resetAndLoad(currentStatus)
        }

        getData()
        fetchData()

    }

    private fun getData() {
        setupRecycler()
        loadPage(currentPage)
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Visits2Status.Idle -> {}
                    is Visits2Status.Loading -> dialog.show()

                    is Visits2Status.GetList -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    com.akhnaton.foodvisits.data.model.saveVisitPhone.Data::class.java
//                                )

                            if (it.data.data.isEmpty()) {
                                binding.llZeroState.visibility = View.VISIBLE
                                binding.rv.visibility = View.GONE
                            } else {
                                binding.llZeroState.visibility = View.GONE
                                binding.rv.visibility = View.VISIBLE
                            }

                            val newItems = it.data.data
                            if (newItems.isEmpty()) {
                                isLastPage = true
                            } else {
                                val startPosition = ordersList.size
                                ordersList.addAll(newItems)
                                Log.d("WHATordersList", ordersList.toString())
                                adapter.notifyItemRangeInserted(
                                    startPosition,
                                    newItems.size
                                )
                                if (newItems.size < pageSize) {
                                    isLastPage = true
                                }
                            }
                            isLoading = false
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.visitsIntent.send(
                                    Visits2Intent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
//                                    findNavController().popBackStack()
                                }
                            )
                        }
                    }

                    is Visits2Status.DeleteOrder -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    com.akhnaton.foodvisits.data.model.saveVisitPhone.Data::class.java
//                                )
                            ordersList.clear()
                            currentPage = 1
                            getData()

                        } else if (it.data.status == 401) {
//                            lifecycleScope.launch {
//                                viewModel.visitsIntent.send(
//                                    Visits2Intent.RefreshToken(
//                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
//                                        SharedPreferencesHelper.getInstance().getUserToken()
//                                    )
//                                )
//                            }
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
//                                    findNavController().popBackStack()
                                }
                            )
                        }
                    }

                    is Visits2Status.GetSalesAndCustomerTypes -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.Data::class.java
                                )
                            orderTypesList = data.sales_types

                        } else if (it.data.status == 401) {
//                            lifecycleScope.launch {
//                                viewModel.visitsIntent.send(
//                                    Visits2Intent.RefreshToken(
//                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
//                                        SharedPreferencesHelper.getInstance().getUserToken()
//                                    )
//                                )
//                            }
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
//                                    findNavController().popBackStack()
                                }
                            )
                        }
                    }

                    is Visits2Status.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            Log.d("WHATRefreshToken", "${it.data.message}")
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                            SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                            getData()
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
                                    SharedPreferencesHelper.getInstance()
                                        .logOut()
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            LoginActivity2::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                }
                            )
                        }
//                        binding.tryAgainButtons.root.visibility = View.GONE

                    }

                    is Visits2Status.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()

                        DialogUtils.showResultDialog(
                            context = requireContext(),
                            message = it.error.toString(),
                            isSuccess = false,
                            showOkButton = true,
                            onOk = {
//                                    findNavController().popBackStack()
                            }
                        )
//                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupRecycler() {
        Log.d("WHATordersList", ordersList.toString())

        adapter = OrderMenuAdapter(ordersList, this)
        val layoutManager =
            LinearLayoutManager(requireContext())
        binding.rv.layoutManager = layoutManager
        binding.rv.adapter = adapter
        binding.rv.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return
                    val visibleItemCount =
                        layoutManager.childCount
                    val totalItemCount =
                        layoutManager.itemCount
                    val firstVisibleItemPosition =
                        layoutManager.findFirstVisibleItemPosition()
                    if (!isLoading &&
                        !isLastPage &&
                        visibleItemCount + firstVisibleItemPosition >= totalItemCount - 3
                    ) {
                        loadNextPage()
                    }
                }
            }
        )
    }

    private fun loadPage(page: Int) {
        isLoading = true
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.GetList(
                    page.toString(),
                    pageSize.toString(),
                    currentStatus,
                    fromDate,
                    toDate,
                    searchText,
                    selectedOrderType
                )
            )
        }
    }

    private fun loadNextPage() {
        currentPage++
        loadPage(currentPage)
    }

    private fun resetAndLoad(status: String) {
        currentStatus = status
        currentPage = 1
        isLastPage = false
        isLoading = false
        ordersList.clear()
        adapter.notifyDataSetChanged()
        loadPage(currentPage)
    }

    override fun onEdit(order: Data) {

        Log.d("ORDER", "Edit ${order.ORIG_SYS_DOCUMENT_REF}")

        val orderJson =
            Gson().toJson(order)

        val bundle = Bundle().apply {
            putString("customerName", order.CUSTOMER_NAME)
            putString("customerCode", order.CUSTOMER_CODE)
            putString("siteAddress", "")
            putString("customerPartySiteId", order.PARTY_SITE_ID)
            putString("saleType", order.ORDER_TYPE)
//            putString("selectedOptions", selectionsJson)
            putString("orderJson", orderJson)
            putString("OrigSysDocumentRef", order.ORIG_SYS_DOCUMENT_REF)
            putBoolean("isEdit", true)
        }

        findNavController().navigate(
            R.id.toInvoice,
            bundle
        )

        // Call Edit API
    }

    override fun onDelete(order: Data) {

        Log.d("ORDER", "Delete ${order.ORIG_SYS_DOCUMENT_REF}")

        // Call Delete API
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = "هل انت متأكد من انك تريد حذف هذا الطلب؟",
            isSuccess = false,
            showYesNoButtons = true,
            onYes = {
                lifecycleScope.launch {
                    viewModel.visitsIntent.send(
                        Visits2Intent.DeleteOrder(
                            order.ORIG_SYS_DOCUMENT_REF
                        )
                    )
                }
            },
        )
    }

    override fun onShowDetails(order: Data) {
        OrderDetailsBottomSheet(
            order.ITEMS_PREVIEW
        ).show(
            childFragmentManager,
            "details"
        )
    }

    private fun TextView.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(1000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_orders_menu,
                container,
                false
            )
        return binding.root
    }
}