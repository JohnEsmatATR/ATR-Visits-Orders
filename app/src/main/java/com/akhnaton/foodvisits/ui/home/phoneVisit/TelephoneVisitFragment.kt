package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentCustomerDetailsBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.BaseActivity
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

class TelephoneVisitFragment : Fragment() {

    companion object {
        private const val TAG = "TelephoneVisitFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentTelephoneVisitBinding
    private lateinit var dialog: AlertDialog

    lateinit var customerName: String
    lateinit var customerCode: String
    lateinit var siteAddress: String
    lateinit var customerPartySiteId: String
    lateinit var saleType: String

    var promotersNotes: String = ""
    var grade: String = ""
    var visitVisibility: String = "N"
    var visitNotes: String = ""
    var phoneVisit: String = "1"
    var dateVisit: Long = 0
    var visitTarget: String = "3"
    var visitActualTarget: String = "3"
    var checkInDate: Long = 0
    var orderType: String = "SALE"
    var customerType: String = "RETAIL"

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
        binding.tvCustomerCode.setText(customerCode)
        binding.tvSiteAddress.setText(siteAddress)

        val positions = listOf(
            getString(R.string.order),
            getString(R.string.collection),
            getString(R.string.negative)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            positions
        )

        binding.etVisitingPosition.setAdapter(adapter)

        binding.etVisitingPosition.setOnItemClickListener { _, _, position, _ ->
            val selectedPosition = positions[position]
            if (selectedPosition == getString(R.string.order)) grade = "A"
            else if (selectedPosition == getString(R.string.collection)) grade = "B"
            else if (selectedPosition == getString(R.string.negative)) grade = "C"
        }

        binding.btnSave.setOnClickListener {
            Log.d("WHATbtnSave", "Clicked")
            checkInDate = getCurrentTimeTimestamp()
            dateVisit = getCurrentDateTimestamp()
            visitActualTarget = binding.etCollectToday.text.toString()
            visitNotes = binding.etVisitNotes.text.toString()
            visitTarget = binding.etObjectiveVisit.text.toString()

            lifecycleScope.launch {
                viewModel.phoneVisitsIntent.send(
                    PhoneVisitsIntent.SaveVisitPhone(
                        SaveVisitPhoneReq(
                            check_in_date = checkInDate,
                            customer_party_site_id = customerPartySiteId,
                            customer_type = customerType,
                            date_visit = dateVisit,
                            grade = grade,
                            order_type = orderType,
                            phone_visit = phoneVisit,
                            promoters_notes = promotersNotes,
                            visit_actual_target = visitActualTarget,
                            visit_notes = visitNotes,
                            visit_target = visitTarget,
                            visit_visibility = visitVisibility
                        )
                    )
                )
            }
        }

//        getCustomerData()
        fetchData()
    }

//    private fun getCustomerData() {
//        lifecycleScope.launch {
//            viewModel.phoneVisitsIntent.send(
//                PhoneVisitsIntent.GetCustomerData(
//                    saleType,
//                    customerCode,
//                    line
//                )
//            )
//        }
//    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.Idle -> {}
                    is PhoneVisitsStatus.Loading -> dialog.show()

                    is PhoneVisitsStatus.SaveVisitPhone -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            Toast.makeText(
//                                requireContext(),
//                                "${it.data.data.visit_id}",
//                                Toast.LENGTH_SHORT
//                            ).show()
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = getString(R.string.saved_successfully),
                                isSuccess = true,
                                seconds = 2,
                                onAutoDismiss = {
                                    val bundle = Bundle().apply {
                                        putString("customerName", customerName)
                                        putString("customerCode", customerCode)
                                        putString("siteAddress", siteAddress)
                                        putString("customerPartySiteId", customerPartySiteId)
                                        putString("saleType", saleType)
                                    }

                                    findNavController().navigate(
                                        R.id.toOrderCreationCycle,
                                        bundle
                                    )
                                }
                            )
                        } else if (it.data.status == 401) {
                            lifecycleScope.launch {
                                viewModel.phoneVisitsIntent.send(
                                    PhoneVisitsIntent.RefreshToken(
                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                                        SharedPreferencesHelper.getInstance().getUserToken()
                                    )
                                )
                            }
                        }
                    }

//                    is PhoneVisitsStatus.GetCustomerData -> {
//                        dialog.dismiss()
//                        if (it.data.status == 200) {
////                            val data =
////                                Gson().fromJson(
////                                    it.data.data,
////                                    Data::class.java
////                                )
//                            setRecycler(it.data.data.customer_address.toMutableList())
//                        } else if (it.data.status == 401) {
//                            lifecycleScope.launch {
//                                viewModel.phoneVisitsIntent.send(
//                                    PhoneVisitsIntent.RefreshToken(
//                                        SharedPreferencesHelper.getInstance().getEmployeeId(),
//                                        SharedPreferencesHelper.getInstance().getUserToken()
//                                    )
//                                )
//                            }
//                        }
//                    }

//                    is PhoneVisitsStatus.RefreshToken -> {
//                        dialog.hide()
//                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
//                                )
////                            getData()
//                        } else {
//
//                        }
////                        binding.tryAgainButtons.root.visibility = View.GONE
//
//                    }

//                    is PhoneVisitsStatus.Error -> {
//                        Log.d(TAG, "fetchData: ${it.error}")
//                        dialog.hide()
//
////                        binding.tryAgainButtons.root.visibility = View.VISIBLE
//                    }

                    else -> {}
                }
            }
        }
    }

    fun getCurrentTimeTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun getCurrentDateTimestamp(): Long {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis / 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_telephone_visit,
                container,
                false
            )
        return binding.root
    }

}