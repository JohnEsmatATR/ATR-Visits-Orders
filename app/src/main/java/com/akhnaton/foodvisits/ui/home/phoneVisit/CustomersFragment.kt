package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.Data
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentCustomersBinding
import com.akhnaton.foodvisits.databinding.FragmentPhoneVisits2Binding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.getValue

class CustomersFragment : Fragment() {
    companion object {
        private const val TAG = "CustomersFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentCustomersBinding
    private lateinit var dialog: AlertDialog

    lateinit var saleType: String

    private var allCustomers =
        mutableListOf<com.akhnaton.foodvisits.data.model.customers.Data>()

    private var displayedCustomers =
        mutableListOf<com.akhnaton.foodvisits.data.model.customers.Data>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saleType =
            arguments?.getString("saleType").toString()

        Log.d("WHAT", "SALE_TYPE: $saleType")

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etSearch.addTextChangedListener {
            filterCustomers(
                it.toString().trim()
            )
        }

        getCustomers()
        fetchData()
    }

    private fun filterCustomers(
        keyword: String
    ) {
        displayedCustomers =
            if (keyword.isBlank()) {
                allCustomers.toMutableList()
            } else {
                allCustomers.filter {
                    it.CUSTOMER_NAME.contains(
                        keyword,
                        true
                    ) ||
                            it.CUSTOMER_CODE.contains(
                                keyword,
                                true
                            )
                }.toMutableList()
            }
        if (displayedCustomers.isEmpty()) {
            binding.llZeroState.visibility =
                View.VISIBLE
            binding.rv.visibility =
                View.GONE
        } else {
            binding.llZeroState.visibility =
                View.GONE
            binding.rv.visibility =
                View.VISIBLE
            setRecycler(displayedCustomers)
        }
    }

    private fun getCustomers() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomers(
                    saleType
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

                    is PhoneVisitsStatus.GetCustomers -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    Data::class.java
//                                )
                            allCustomers =
                                it.data.data.toMutableList()
                            displayedCustomers =
                                allCustomers.toMutableList()
                            if (displayedCustomers.isEmpty()) {
                                binding.llZeroState.visibility =
                                    View.VISIBLE
                                binding.rv.visibility =
                                    View.GONE
                            } else {
                                binding.llZeroState.visibility =
                                    View.GONE
                                binding.rv.visibility =
                                    View.VISIBLE
                                setRecycler(displayedCustomers)
                            }

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
                                            LoginActivity::class.java
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
        list: MutableList<com.akhnaton.foodvisits.data.model.customers.Data>
    ) {
        val adapter = CustomersAdapter(list, { customer ->

            val bundle = Bundle().apply {
                putString("saleType", saleType)
                putString("customerCode", customer.CUSTOMER_CODE)
                putString("line", customer.TEAM_NAME)
                putString("customerName", customer.CUSTOMER_NAME)
            }

            findNavController().navigate(
                R.id.toCustomerDetails,
                bundle
            )
        })

        binding.rv.layoutManager =
            LinearLayoutManager(
                requireContext(),
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
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_customers,
                container,
                false
            )
        return binding.root
    }
}