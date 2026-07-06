package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.app.AlertDialog
import android.content.Intent
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getCustomerData.Data
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentCustomerDetailsBinding
import com.akhnaton.foodvisits.databinding.FragmentCustomersBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.getValue

class CustomerDetailsFragment : Fragment() {

    companion object {
        private const val TAG = "CustomerDetailsFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentCustomerDetailsBinding
    private lateinit var dialog: AlertDialog

    lateinit var saleType: String
    lateinit var customerCode: String
    lateinit var line: String
    lateinit var customerName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saleType =
            arguments?.getString("saleType").toString()
        customerCode =
            arguments?.getString("customerCode").toString()
        line =
            arguments?.getString("line").toString()
        customerName =
            arguments?.getString("customerName").toString()

        Log.d("WHAT", "SALE_TYPE: $saleType")

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        MainActivity.binding.navView2.visibility = View.GONE

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvCustomerName.setText("${getString(R.string.client)}: $customerName")

        getCustomerData()
        fetchData()

    }

    private fun getCustomerData() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomerData(
                    saleType,
                    customerCode,
                    line
                )
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.Idle -> dialog.show()
                    is PhoneVisitsStatus.Loading -> dialog.show()

                    is PhoneVisitsStatus.GetCustomerData -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    Data::class.java
                                )
                            setRecycler(data.customer_address.toMutableList())
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.phoneVisitsIntent.send(
                                    PhoneVisitsIntent.RefreshToken(
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

                    is PhoneVisitsStatus.RefreshToken -> {
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

                    is PhoneVisitsStatus.Error -> {
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
        list: MutableList<CustomerAddres>
    ) {
        val adapter =
            CustomerDataAdapter(
                object : CustomerDataAdapter.OnItemClickListener {

                    override fun onClick(item: CustomerAddres) {

                        Log.d("WHATclick", "HII CLICK")

                        val bundle = Bundle().apply {
                            putString("customerName", item.CUSTOMER_NAME)
                            putString("customerCode", item.CUSTOMER_CODE)
                            putString("siteAddress", item.SITE_ADDRESS)
                            putString("customerPartySiteId", item.PARTY_SITE_ID)
                            putString("saleType", saleType)
                        }

                        findNavController().navigate(
                            R.id.toTelephoneVisit,
                            bundle
                        )
//                        val intent = Intent(
//                            requireContext(),
//                            CustomersActivity::class.java
//                        )
//
//                        intent.putExtra(
//                            "CARD_NAME",
//                            item
//                        )
//
//                        startActivity(intent)
                    }
                }
            )

        adapter.setList(list)

        binding.rv.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rv.adapter = adapter
        binding.rv.itemAnimator = DefaultItemAnimator()
    }

    private fun getData() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomers(saleType)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_customer_details,
                container,
                false
            )
        return binding.root
    }

}