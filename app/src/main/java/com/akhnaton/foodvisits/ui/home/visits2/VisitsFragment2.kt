package com.akhnaton.foodvisits.ui.home.visits2

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.SelectLists
import com.akhnaton.foodvisits.data.model.getVisitPlan.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.databinding.FragmentVisits2Binding
import com.akhnaton.foodvisits.shared.DateUtils
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.getDistanceFromCurrentLocation
import com.akhnaton.foodvisits.shared.openLocationInMap
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.order.OrderCreationCycleAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.CustomersAdapter
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue

class VisitsFragment2 : Fragment() {

    companion object {
        private const val TAG = "VisitsFragment2"
    }

    private val viewModel: Visits2ViewModel by viewModels()
    private lateinit var binding: FragmentVisits2Binding
    private lateinit var dialog: AlertDialog

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var customerLatitude: Double? = 0.0
    var customerLongitude: Double? = 0.0
    private var currentDistanceMeters: Float = 0f

    private var allCustomers = mutableListOf<CustomerVisitPlan>()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        MainActivity.binding.navView2.visibility = View.VISIBLE

//        binding.tvDay.setText(DateUtils.getTodayDayName())
//        binding.tvDate.setText(DateUtils.getTodayDate())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation.addOnSuccessListener { currentLocation: Location? ->
            if (currentLocation != null && customerLatitude != null && customerLongitude != null) {
                val distanceKm = getDistanceFromCurrentLocation(
                    currentLocation = currentLocation,
                    targetLat = customerLatitude!!,
                    targetLng = customerLongitude!!
                )

                val results = FloatArray(1)

                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    customerLatitude!!,
                    customerLongitude!!,
                    results
                )

                currentDistanceMeters = results[0]
                Log.d(
                    "Distance", "%.2f KM".format(distanceKm)
                )
            }
        }

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )

        binding.swipeRefresh.setOnRefreshListener {
            loadVisitPlan()
        }

        binding.etSearch.addTextChangedListener {
            filterCustomers(
                it.toString().trim()
            )
        }

        loadVisitPlan()
        fetchData()

    }

    private fun filterCustomers(keyword: String) {
        val filteredList =
            if (keyword.isBlank()) {
                allCustomers
            } else {
                allCustomers.filter {
                    it.customer_name.contains(keyword, true) ||
                            it.customer_code.contains(keyword, true) ||
                            it.customer_address.contains(keyword, true)
                }.toMutableList()
            }
        if (filteredList.isEmpty()) {
            binding.rv.visibility = View.GONE
            binding.llZeroState.visibility = View.VISIBLE
        } else {
            binding.rv.visibility = View.VISIBLE
            binding.llZeroState.visibility = View.GONE
        }

        setRecycler(filteredList)
    }

    private fun loadVisitPlan() {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.GetVisitPlan
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Visits2Status.Idle -> {}
                    is Visits2Status.Loading -> {
                        if (!binding.swipeRefresh.isRefreshing) {
                            dialog.show()
                        }
                    }

                    is Visits2Status.GetVisitPlan -> {
                        dialog.dismiss()
                        binding.swipeRefresh.isRefreshing = false
                        if (it.data.status == 200) {
                            binding.tvDate.setText(DateUtils.getTodayDate())
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.getVisitPlan.Data::class.java
                                )
                            binding.tvDay.setText(data.day)
                            binding.tvDate.setText(data.date)
                            allCustomers = data.customer_visit_plan.toMutableList()
                            setRecycler(allCustomers)
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
                        binding.swipeRefresh.isRefreshing = false
                        if (it.data.status == 200) {
                            Log.d("WHATRefreshToken", "${it.data.message}")
                            val data =
                                Gson().fromJson(
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
                        binding.swipeRefresh.isRefreshing = false

//                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setRecycler(
        list: MutableList<CustomerVisitPlan>
    ) {
        if (list.size == 0) {
            binding.llZeroState.visibility = View.VISIBLE
            binding.rv.visibility = View.GONE
        } else {
            binding.llZeroState.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE
        }
        val adapter = Visits2Adapter(object : Visits2Adapter.OnItemClickListener {
            override fun onClick(item: CustomerVisitPlan) {
                Log.d("WHAT", "onClick: $item")

                val bundle = Bundle().apply {
                    putString("customerName", item.customer_name)
                    putString("customerCode", item.customer_code)
                    putString("siteAddress", item.customer_address)
                    putString("customerPartySiteId", item.customer_party_site_id)
                    putString("saleType", item.customer_order_type)
                    putDouble("customerLatitude", item.customer_latitude)
                    putDouble("customerLongitude", item.customer_longitude)
                    putInt("validGpsRange", item.valid_gps_range)
                }

                findNavController().navigate(
                    R.id.toGpsVisit,
                    bundle
                )
            }

            override fun onLocationClick(item: CustomerVisitPlan) {
                Log.d("Visits", "Location clicked: ${item.customer_name}")
                openLocationInMap(
                    requireContext(), item.customer_latitude, item.customer_longitude
                )
            }
        })

        adapter.setList(list)

        binding.rv.layoutManager =
            LinearLayoutManager(
                requireContext(),
            )

        binding.rv.adapter = adapter
        binding.rv.itemAnimator = DefaultItemAnimator()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_visits2,
                container,
                false
            )
        return binding.root
    }

}