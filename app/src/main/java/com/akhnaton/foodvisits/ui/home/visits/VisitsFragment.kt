package com.akhnaton.foodvisits.ui.home.visits

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.db.model.AppDatabase
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SharedPrefsHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class VisitsFragment : Fragment(), PlanViewHolder.OnSelectEmployeeClickListener,
    View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: VisitsViewModel
    private lateinit var binding: FragmentVisitsBinding
    private val mAdapter: PlanAdapter = PlanAdapter()
    private var mList: MutableList<CustomerVisitPlan> = ArrayList()
    private val versionName = BuildConfig.VERSION_NAME
    private var visitsPlan: VisitsPlan? = null

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private lateinit var dialog: AlertDialog
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)
        viewModel =
            ViewModelProvider(this, VisitsViewModelFactory(context!!))[VisitsViewModel::class.java]

        dialog = ProgressDialogHelper().showAlertProgress(
            requireActivity(),
            "Loading..."
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.tryAgainButtons.tryAgain.setOnClickListener(this)
        setupRecycler()
        setupSearchView()
        fetchData()
        getUserLocationAndFetchPlan()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mList.clear()
        getUserLocationAndFetchPlan()
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
        binding.searchView.setQuery("", false)
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.statusVisit.collect {
                when (it) {
                    is VisitsStatus.Idle -> dialog.show()
                    is VisitsStatus.Loading -> dialog.show()


                    is VisitsStatus.Plan -> {

                        dialog.hide()
                        mList.clear()
                        visitsPlan = it.data
                        Log.d(TAG, "fetchData: ${it.data.data.customer_visit_plan}")
                        mList.addAll(it.data.data.customer_visit_plan)

                        binding.day.text = it.data.data.day
                        binding.date.text = it.data.data.date


                        val sortedList = mList
                            .sortedWith(compareBy(

                                nullsLast()
                            ) { customer ->
                                val lat = customer.customer_latitude?.toDoubleOrNull()
                                val lng = customer.customer_longitude?.toDoubleOrNull()
                                if (userLatitude != null && userLongitude != null && lat != null && lng != null) {
                                    calculateDistance(userLatitude!!, userLongitude!!, lat, lng)
                                } else {
                                    null
                                }
                            })


                        mList.clear()
                        mList.addAll(sortedList)


                        Log.d(TAG, "Sorted customer list by distance:")
                        mList.forEachIndexed { index, customer ->
                            val lat = customer.customer_latitude?.toDoubleOrNull()
                            val lng = customer.customer_longitude?.toDoubleOrNull()
                            val distance =
                                if (userLatitude != null && userLongitude != null && lat != null && lng != null) {
                                    calculateDistance(userLatitude!!, userLongitude!!, lat, lng)
                                } else null
                            Log.d(
                                "TAGGGGG",
                                "${index + 1}- ${customer.customer_name} (ID: ${customer.customer_party_site_id}) => Distance: ${distance ?: "N/A"} m"
                            )
                        }

                        if (mList.isEmpty()) {
                            binding.recPlan.visibility = View.GONE
                            binding.txtNoData.visibility = View.VISIBLE
                        } else {
                            binding.recPlan.visibility = View.VISIBLE
                            binding.txtNoData.visibility = View.GONE
                            setAdapterData(mList)
                        }

                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is VisitsStatus.Error -> {
                        dialog.hide()
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }

    private fun getUserLocationAndFetchPlan() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                userLatitude = it.latitude
                userLongitude = it.longitude
                Log.d(TAG, "User Location -> Latitude: $userLatitude, Longitude: $userLongitude")


            }
        }
    }
    private fun setupRecycler() {
        binding.recPlan.adapter = mAdapter
        binding.recPlan.apply {
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun setAdapterData(data: List<CustomerVisitPlan>) {
        mAdapter.setPlan(data, this)
    }

    override fun onSelectEmployeeClickListener(data: CustomerVisitPlan, position: Int) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).visitTimerDao()
            val openVisits = dao.getAllVisitTimers()

            val isSameVisitOpen = openVisits.any { it.customerPartySiteId == data.customer_party_site_id }
            if (openVisits.isEmpty() || isSameVisitOpen) {
                val tsLong = System.currentTimeMillis() / 1000
                Log.d(TAG, "onSelectEmployeeClickListener: ${tsLong}")
                startActivity(
                    Intent(requireActivity(), VisitsDetailsActivity::class.java)
                        .putExtra("customerPartySiteId", data.customer_party_site_id)
                        .putExtra("time", tsLong.toString())
                        .putExtra("customerSiteData", data)
                        .putExtra("orderType", data.customer_order_type)
                        .putExtra("customerTypePosition", data.customer_type)
                        .putExtra("customer_name", data.customer_name)
                )
            } else {

                showOpenVisitsDialog()
            }
        }

    }
    private fun showOpenVisitsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("تنبيه")
            .setMessage("⚠️ يوجد زيارات مفتوحة. برجاء إغلاقها قبل بدء زيارة جديدة.")
            .setPositiveButton("حسنًا", null)
            .show()
    }


    override fun onClick(p0: View?) {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = mList.filter {
                    it.customer_name.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_party_site_id.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_order_type?.contains(newText.orEmpty(), ignoreCase = true) == true
                }
                setAdapterData(filteredList)
                return true
            }
        })
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocationAndFetchPlan()
        } else {
            lifecycleScope.launch {
                viewModel.visitsIntent.send(
                    VisitsIntent.GetPlan(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken()
                    )
                )
            }
        }
    }
    private fun calculateDistance(
        userLat: Double,
        userLng: Double,
        clientLat: Double,
        clientLng: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(userLat, userLng, clientLat, clientLng, result)
        return result[0]
    }

}

