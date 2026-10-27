package com.akhnaton.foodvisits.ui.home.order

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentOrderCreationCycleBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.phoneVisit.CustomerDataAdapter
import com.akhnaton.foodvisits.ui.home.order.OrderCreationCycleAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import com.google.gson.Gson
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
    lateinit var fragment: String
    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false
    private var selectLists =
        mutableListOf<SelectLists>()

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleTopBottomKeyboard()

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
        fragment =
            arguments?.getString("fragment").toString()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        //MainActivity.binding.navView2.visibility = View.GONE

        binding.btnBack.setOnClickListener {
            if (fragment == "Telephone")
                findNavController().popBackStack(
                    R.id.visitPhoneFragment,
                    false
                )
            else if (fragment == "Gps")
                goBackToVisits()
        }

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (fragment == "Telephone")
                    findNavController().popBackStack(
                        R.id.visitPhoneFragment,
                        false
                    )
                else if (fragment == "Gps")
                    goBackToVisits()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        binding.tvCustomerName.setText(customerName)
        binding.tvCustomerCode.setText("${getString(R.string.code)}: $customerCode")
        binding.tvSaleType.setText("${getString(R.string.sale_type)}: $saleType")

        binding.btnContinue.setOnClickListener {
            val missingRequired =
                selectLists.any {
                    it.required == 1 &&
                            it.selectedValue.isNullOrEmpty()
                }
            if (missingRequired) {
                DialogUtils.showResultDialog(
                    requireContext(),
                    "برجاء استكمال جميع الاختيارات المطلوبة",
                    false,
                    showOkButton = true,
                    isDismissable = true
                )
                return@setOnClickListener
            }

            val selectedOptions =
                selectLists.mapNotNull {

                    if (
                        !it.selectedId.isNullOrEmpty()
                    ) {

                        SelectedOption(
                            key = it.select,
                            id = it.selectedId!!,
                            value = it.selectedValue ?: ""
                        )

                    } else {
                        null
                    }
                }

            val selectionsJson =
                Gson().toJson(selectedOptions)

            val bundle = Bundle().apply {
                putString("customerName", customerName)
                putString("customerCode", customerCode)
                putString("siteAddress", siteAddress)
                putString("customerPartySiteId", customerPartySiteId)
                putString("saleType", saleType)
                putString("selectedOptions", selectionsJson)
                putBoolean("isEdit", false)
            }

            Log.d("WHATselectionsJson", selectionsJson.toString())

            findNavController().navigate(
                R.id.toInvoice,
                bundle
            )
        }

        getStartOrderData()
        fetchData()

    }

    private fun handleTopBottomKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
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
        val nav = findNavController()
        if (!nav.popBackStack(R.id.visitsFragment, false)) {
            nav.navigate(R.id.visitsFragment)
        }
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

    @SuppressLint("LongLogTag")
    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Order2Status.Idle -> {}
                    is Order2Status.Loading -> dialog.show()

                    is Order2Status.GetStartOrderData -> {
                        dialog.dismiss()
                        if (it.data.status != 401) hasRetriedAfterRefresh = false
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    Data::class.java
                                )
                            selectLists =
                                data.select_lists.toMutableList()

                            setRecycler(selectLists)
                        } else if (it.data.status == 401) {
                            if (hasRetriedAfterRefresh) {
                                hasRetriedAfterRefresh = false
                                pendingRetry = null
                                showSessionExpired(it.data.message)
                            } else {
                                pendingRetry = { getStartOrderData() }
                                sendRefreshToken()
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
                        Log.d(TAG, "refreshToken status=${it.data.status} message=${it.data.message}")
                        if (it.data.status == 200) {


                            val tokenData = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                            )
                            SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                            hasRetriedAfterRefresh = true
                            val retry = pendingRetry
                            pendingRetry = null
                            retry?.invoke()
                        } else {
                            pendingRetry = null
                            hasRetriedAfterRefresh = false
                            showSessionExpired(it.data.message)
                        }
                    }

                    is Order2Status.Error -> {
                        pendingRetry = null
                        hasRetriedAfterRefresh = false
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()

//                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }
    private fun sendRefreshToken() {
        lifecycleScope.launch {
            viewModel.orderIntent.send(
                Order2Intent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
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

    private fun showSessionExpired(message: String?) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message.orEmpty(),
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(
                    Intent(requireContext(), LoginActivity2::class.java)
                )
                requireActivity().finishAffinity()
            }
        )
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