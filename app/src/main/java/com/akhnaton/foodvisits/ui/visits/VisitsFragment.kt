package com.akhnaton.foodvisits.ui.visits

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.ProgressDialog
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch

class VisitsFragment : Fragment(), PlanViewHolder.OnSelectEmployeeClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private val viewModel: VisitsViewModel by viewModels()
    private lateinit var binding: FragmentVisitsBinding
    private val mAdapter: PlanAdapter = PlanAdapter()
    private var mList: List<CustomerVisitPlan> = ArrayList()
    private val versionName = BuildConfig.VERSION_NAME


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)

        binding.day.text = ConvertDate.getDay()
        binding.date.text = ConvertDate.getDate()
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }

        setupRecycler()
        fetchData()
        return binding.root
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is VisitsStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is VisitsStatus.Loading -> ProgressDialog().showProgress(requireActivity())
                        .show()

                    //Get Order Type
                    is VisitsStatus.Plan -> {
                        ProgressDialog().showProgress(requireActivity()).hide()
                        setAdapterData(it.data.data.customer_visit_plan)
                        Log.d(TAG, "fetchData: Data ${it.data.data.customer_visit_plan}")
                        mList = it.data.data.customer_visit_plan
                    }

                    is VisitsStatus.Error -> Log.d(TAG, "fetchData: Error:  ${it.error}")
                }
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
        val tsLong = System.currentTimeMillis() / 1000

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val manager: LocationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager;

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                ProgressDialog().gpsAlert(requireActivity())
            } else {
                startActivity(
                    Intent(
                        requireActivity(),
                        VisitsDetailsActivity::class.java
                    )
                        .putExtra("customerPartySiteId", mList[position].customer_party_site_id)
                        .putExtra("time", tsLong.toString())
                        .putExtra("customerSiteData", mList[position])
                        .putExtra("orderType", mList[position].customer_order_type)
                        .putExtra("customerTypePosition", mList[position].customer_type)
                )
            }

        } else {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                    )
                }
            }
        }
    }


}

