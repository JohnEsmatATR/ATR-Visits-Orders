package com.akhnaton.foodvisits.ui.home.returns

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getPriceLists.DamagePriceId
import com.akhnaton.foodvisits.data.model.getPriceLists.PriceId
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectedOption
import com.akhnaton.foodvisits.data.model.startReturnData.Data
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsIntent
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsStatus
import com.akhnaton.foodvisits.databinding.FragmentReturnOptionsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.order.OrderCreationCycleAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ReturnOptionsFragment : Fragment() {

    companion object {
        private const val TAG = "ReturnOptionsFragment"
    }

    private val viewModel: ReturnsViewModel by viewModels()
    private lateinit var binding: FragmentReturnOptionsBinding
    private lateinit var dialog: AlertDialog
    private lateinit var backPressedCallback: OnBackPressedCallback

    private lateinit var customerPartySiteId: String
    private lateinit var saleType: String
    private lateinit var orderId: String
    private lateinit var storeId: String

    private var selectedPriceList: PriceId? = null
    private var selectedDamagePriceList: DamagePriceId? = null
    private var returnData: Data? = null
    private var selectLists = mutableListOf<SelectLists>()
    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboardInsets()

        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()
        orderId = arguments?.getString("orderId").toString()
        storeId = arguments?.getString("storeId").toString()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backPressedCallback
        )

        binding.btnContinue.setOnClickListener {
            val data = returnData
            val priceList = selectedPriceList
            val damagePriceList = selectedDamagePriceList
            if (data == null || priceList == null || damagePriceList == null) {
                return@setOnClickListener
            }

            val missingRequired = selectLists.any {
                it.required == 1 && it.selectedValue.isNullOrEmpty()
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

            val selectedOptions = selectLists.mapNotNull {
                if (!it.selectedId.isNullOrEmpty()) {
                    SelectedOption(
                        key = it.select,
                        id = it.selectedId!!,
                        value = it.selectedValue ?: ""
                    )
                } else {
                    null
                }
            }

            val bundle = Bundle().apply {
                putString("customerPartySiteId", customerPartySiteId)
                putString("saleType", saleType)
                putString("orderId", orderId)
                putString("storeId", storeId)
                putString("returnData", Gson().toJson(data))
                putString("priceList", Gson().toJson(priceList))
                putString("damagePriceList", Gson().toJson(damagePriceList))
                putString("selectedOptions", Gson().toJson(selectedOptions))
            }

            findNavController().navigate(R.id.toReturns, bundle)
        }

        fetchData()

        if (returnData == null) {
            getPriceLists()
        } else {
            setRecycler(selectLists)
        }
    }

    private fun getPriceLists() {
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.GetPriceLists(customerPartySiteId, saleType)
            )
        }
    }

    private fun startReturnData() {
        val damagePriceList = selectedDamagePriceList ?: return
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.StartReturnData(orderId, damagePriceList.PRICE_LIST_ID)
            )
        }
    }

    private fun refreshToken(message: String, retry: () -> Unit) {
        if (hasRetriedAfterRefresh) {
            hasRetriedAfterRefresh = false
            pendingRetry = null
            showSessionExpired(message)
            return
        }
        pendingRetry = retry
        lifecycleScope.launch {
            viewModel.returnsIntent.send(
                ReturnsIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
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
                        if (it.data.status != 401) hasRetriedAfterRefresh = false
                        if (it.data.status == 200) {
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.getPriceLists.Data::class.java
                            )
                            selectedPriceList = data.price_list_id[0]
                            selectedDamagePriceList = data.damage_price_list_id[0]
                            startReturnData()
                        } else if (it.data.status == 401) {
                            refreshToken(it.data.message) { getPriceLists() }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message, false,
                                showOkButton = true,
                            )
                        }
                    }

                    is ReturnsStatus.StartReturnData -> {
                        dialog.dismiss()
                        if (it.data.status != 401) hasRetriedAfterRefresh = false
                        if (it.data.status == 200) {
                            if (returnData == null) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    Data::class.java
                                )
                                returnData = data
                                selectLists = data.select_list.toMutableList()
                            }
                            setRecycler(selectLists)
                        } else if (it.data.status == 401) {
                            refreshToken(it.data.message) { getPriceLists() }
                        } else {
                            DialogUtils.showResultDialog(
                                requireContext(), it.data.message, false,
                                showOkButton = true,
                            )
                        }
                    }

                    is ReturnsStatus.RefreshToken -> {
                        dialog.hide()
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
                            hasRetriedAfterRefresh = true
                            pendingRetry = null
                            showSessionExpired(it.data.message)
                        }
                    }

                    is ReturnsStatus.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        pendingRetry = null
                        hasRetriedAfterRefresh = false
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
        }
    }

    private fun setRecycler(list: MutableList<SelectLists>) {
        val adapter = OrderCreationCycleAdapter(
            object : OrderCreationCycleAdapter.OnItemClickListener {
                override fun onClick(item: SelectLists) {
                }
            }
        )

        adapter.setList(list)

        binding.rvOptions.layoutManager = LinearLayoutManager(requireContext())
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

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(imeInsets.bottom, systemBars.bottom)
            )
            insets
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_return_options, container, false
        )
        return binding.root
    }
}