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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentOrderCreationCycleBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.phoneVisit.CustomerDataAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.OrderCreationCycleAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class OrderCreationCycleFragment : Fragment() {

    companion object {
        private const val TAG = "OrderCreationCycleFragment"
    }

    private val viewModel: Order2ViewModel by viewModels()
    private lateinit var binding: FragmentOrderCreationCycleBinding
    private lateinit var dialog: AlertDialog

    lateinit var customerName: String
    lateinit var customerCode: String
    lateinit var siteAddress: String
    lateinit var customerPartySiteId: String
    lateinit var saleType: String

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

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        binding.tvCustomerName.setText(customerName)
        binding.tvCustomerCode.setText("${getString(R.string.code)}: $customerCode")
        binding.tvSaleType.setText("${getString(R.string.sale_type)}: $saleType")

        getStartOrderData()
        fetchData()

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
                            setRecycler(it.data.data.select_lists.toMutableList())
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
        list: MutableList<SelectLists>
    ) {
        val adapter =
            OrderCreationCycleAdapter(
                object : OrderCreationCycleAdapter.OnItemClickListener {
                    override fun onClick(item: SelectLists) {

                    }
                }
            )

        adapter.setList(list)

        binding.rvOptions.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rvOptions.adapter = adapter
        binding.rvOptions.itemAnimator = DefaultItemAnimator()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_order_creation_cycle,
                container,
                false
            )
        return binding.root
    }
}