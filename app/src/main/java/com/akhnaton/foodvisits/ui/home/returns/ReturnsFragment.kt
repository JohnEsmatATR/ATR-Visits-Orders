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
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getPriceLists.DamagePriceId
import com.akhnaton.foodvisits.data.model.getPriceLists.PriceId
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsIntent
import com.akhnaton.foodvisits.data.statusValue.returns.ReturnsStatus
import com.akhnaton.foodvisits.databinding.FragmentInvoice2Binding
import com.akhnaton.foodvisits.databinding.FragmentReturnsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.InvoiceFragment2
import com.akhnaton.foodvisits.ui.home.order.Order2ViewModel
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
    lateinit var selectedDamagePriceList: DamagePriceId
    lateinit var selectedPriceList: PriceId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()

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

        getPriceLists()
        fetchData()

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
//                            val data = Gson().fromJson(
//                                it.data.data, Data::class.java
//                            )
                            selectedPriceList = it.data.data.price_list_id[0]
                            selectedDamagePriceList = it.data.data.damage_price_list_id[0]

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