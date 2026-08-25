package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.promoterSaveStock.Item
import com.akhnaton.foodvisits.data.model.saveVisitGps.Data
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.databinding.FragmentGpsVisitBinding
import com.akhnaton.foodvisits.databinding.FragmentInventoryBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.inventory.ProductInventoryAdapter
import com.akhnaton.foodvisits.ui.home.visits2.Visits2ViewModel
import com.akhnaton.foodvisits.ui.home.visits2.Visits2ViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    companion object {
        private const val TAG = "InventoryFragment"
    }

    private lateinit var viewModel: Visits2ViewModel
    private lateinit var binding: FragmentInventoryBinding
    private lateinit var dialog: AlertDialog
    private lateinit var adapter: ProductInventoryAdapter

    lateinit var customerCode: String
    lateinit var customerPartySiteId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getBundle()
        init()
        callApis()
        observeData()
        setupClicks()

    }

    fun getBundle() {
        customerCode = arguments?.getString("customerCode").toString()
        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
    }


    fun init() {
        viewModel = ViewModelProvider(
            this,
            Visits2ViewModelFactory(requireContext())
        )[Visits2ViewModel::class.java]

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()
    }


    fun callApis() {
        callGetItemData()
    }

    private fun setupRecyclerView(items: List<com.akhnaton.foodvisits.data.model.promoterGetItemData.Data>) {
        adapter = ProductInventoryAdapter { product ->
//            saveProductChanges(product)
            prepareRequest(false)
        }

        adapter.setData(items)
        binding.recyclerProducts.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = this@InventoryFragment.adapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun setupClicks() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSendInventory.setOnClickListener {
            prepareRequest(true)
        }
    }

    fun callGetItemData() {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.PromoterGetItemData(
                    customerCode,
                    customerPartySiteId
                )
            )
        }
    }

    fun prepareRequest(isAll: Boolean) {
        when (isAll) {
            true -> {
                val items = adapter.getData()
                val requestedItems = convertAdapterDataToRequestItems(items)
                callSaveStockAPI(requestedItems)
            }

            false -> {
                val items = adapter.getData().filter { it.hasChanges == true }
                val requestedItems = convertAdapterDataToRequestItems(items)
                callSaveStockAPI(requestedItems)
            }
        }
    }

    fun convertAdapterDataToRequestItems(items: List<com.akhnaton.foodvisits.data.model.promoterGetItemData.Data>): ArrayList<Item> {
        val requestItems = ArrayList<Item>()
        items.forEach {
            if (it.hasChanges) {
                requestItems.add(
                    Item(
                        it.inventory_item_id.toInt(),
                        if (it.writtenPrice != null) it.writtenPrice.toDouble() else 0.0,
                        if (it.writtenQuantity != null) it.writtenQuantity.toInt() else 0,
                        if (it.writtenReturned != null) it.writtenReturned.toInt() else 0
                    )
                )
            }
        }
        return requestItems
    }

    fun callSaveStockAPI(items: List<Item>) {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.PromoterSaveStock(
                    com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq(
                        customerCode,
                        items,
                        customerPartySiteId
                    )
                )
            )
        }
    }


    fun observeData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Visits2Status.Idle -> {}
                    is Visits2Status.Loading -> dialog.show()

                    is Visits2Status.PromoterGetItemData -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            setupRecyclerView(it.data.data)
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

                    is Visits2Status.PromoterSaveStock -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = true,
                                showOkButton = true,
                                onOk = {
                                    findNavController().popBackStack()
                                }
                            )
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

                    is Visits2Status.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            Log.d("WHATRefreshToken", "${it.data.message}")
                            val data = Gson().fromJson(
                                it.data.data,
                                com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                            )
                            SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
//                            getData()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_inventory, container, false
        )
        return binding.root
    }
}