package com.akhnaton.foodvisits.ui.visits

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.visits.CustomerSite
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.ProgressDialog
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch

class VisitsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private val viewModel: VisitsViewModel by viewModels()
    private lateinit var binding: FragmentVisitsBinding
    private val versionName = BuildConfig.VERSION_NAME
    private var orderType: String = ""
    private val customerType: MutableList<String> = ArrayList()
    private val linesName: MutableList<String> = ArrayList()
    private val mainLine: MutableList<String> = ArrayList()
    private var customerTypeList: List<CustomerType> = ArrayList()
    private var linesList: List<LinesUsers> = ArrayList()
    private var mainList: List<MainCustomerLine> = ArrayList()
    private var customerSiteList: List<SitesData> = ArrayList()
    private var customerTypePosition: String = ""
    private var customerLinePosition: String = ""
    private var mainCustomerLinePosition: String = ""
    private var customerPartySiteId: String = ""
    private lateinit var mListPassedData: SitesData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)

        binding.addVisit.setOnClickListener(this)

        getOrderTypeItemClick()
        getCustomerTypeItemClick()
        getLinesItemClick()
        getMainLineItemClick()
        getData()
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
                        setAdapter(binding.orderType, it.data.data.user_order_type.toMutableList())
                    }

                    //Get User Customer Type
                    is VisitsStatus.GetCustomerType -> {
                        val mCustomerList = it.data.data.user_customer_type
                        customerTypeList = mCustomerList
                        mCustomerList.forEach { customer ->
                            customerType.add(customer.customer_name)
                        }
                        setAdapter(binding.customerType, customerType)
                    }

                    //Get User Lines
                    is VisitsStatus.GetLines -> {
                        linesName.clear()
                        binding.lines.text.clear()
                        linesList = it.data.data.user_lines
                        linesList.forEach { line -> linesName.add(line.line_name) }
                        setAdapter(binding.lines, linesName)
                    }

                    //Get Main Customer Lines
                    is VisitsStatus.GetCustomerLines -> {
                        mainLine.clear()
                        binding.mainLine.text.clear()
                        mainList = it.data.data.main_customer_line
                        it.data.data.main_customer_line.forEach { line -> mainLine.add(line.customer_name) }
                        setAdapter(binding.mainLine, mainLine)
                    }

                    is VisitsStatus.GetCustomersSite -> {
                        val mSitesList: MutableList<String> = ArrayList()
                        customerSiteList = it.data.data.customer_site

                        it.data.data.customer_site.forEach { sites -> mSitesList.add(sites.customer_name) }
                        getCustomersSiteSpinner(mSitesList)
                    }
                    //Catch Error
                    is VisitsStatus.Error -> Log.d(TAG, "fetchData: ${it.error}")
                    is VisitsStatus.SaveVisits -> TODO()
                }
            }
        }
    }


    //Get Order Type
    private fun getOrderTypeItemClick() {
        binding.orderType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            val selected = adapter?.getItemAtPosition(position)
            orderType = selected.toString()
            binding.customerLayout.visibility = View.VISIBLE
            Log.d(TAG, "getOrderTypeItemClick: $orderType")
        }
    }

    //Get User Customer Type
    private fun getCustomerTypeItemClick() {
        binding.customerType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            customerTypePosition = customerTypeList[position].customer_type_id
            lifecycleScope.launch {
                viewModel.visitsIntent.send(
                    VisitsIntent.GetLines(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerTypePosition, orderType
                    )
                )
            }
        }
    }

    //Get User Lines
    private fun getLinesItemClick() {
        binding.lines.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.mainLineLayout.visibility = View.VISIBLE

            customerLinePosition = linesList[position].line_id

            lifecycleScope.launch {
                viewModel.visitsIntent.send(
                    VisitsIntent.GetCustomerLines(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerTypePosition, orderType, customerLinePosition
                    )
                )
            }
        }
    }

    //Get Main Customer Lines
    private fun getMainLineItemClick() {
        binding.mainLine.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.sitesLineLayout.visibility = View.VISIBLE

            mainCustomerLinePosition = mainList[position].customer_code

            lifecycleScope.launch {
                viewModel.visitsIntent.send(
                    VisitsIntent.GetCustomersSite(
                        version = versionName,
                        token = SharedPreferencesHelper.getInstance().getUserToken(),
                        customerType = customerTypePosition,
                        orderType = orderType,
                        lineId = customerLinePosition,
                        customerCode = mainCustomerLinePosition
                    )
                )
            }
        }
    }


    private fun getCustomersSiteSpinner(mList: MutableList<String>) {
        binding.sitesSpinner.setItems(mList.toTypedArray())
        binding.sitesSpinner.setTitle("Choose Customer Site")
        binding.sitesSpinner.setExpandTint(R.color.colorAccent)

        binding.sitesSpinner.setOnItemClickListener {
            customerPartySiteId = customerSiteList[it].customer_party_site_id
            mListPassedData = customerSiteList[it]
            binding.addVisit.visibility = View.VISIBLE
        }
    };

    //Set default adapter
    private fun setAdapter(binding: AutoCompleteTextView, list: MutableList<String>) {
        val customerTypeAdapter = ArrayAdapter(
            requireActivity(),
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            list
        )
        customerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.setAdapter(customerTypeAdapter)
    }

    private fun getData() {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )

            viewModel.visitsIntent.send(
                VisitsIntent.GetCustomerType(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    override fun onClick(v: View?) {
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
            startActivity(
                Intent(
                    requireActivity(),
                    VisitsDetailsActivity::class.java
                )
                    .putExtra("customerPartySiteId", customerPartySiteId)
                    .putExtra("time", tsLong.toString())
                    .putExtra("customerSiteData", mListPassedData)
                    .putExtra("orderType", orderType)
                    .putExtra("customerTypePosition", customerTypePosition)
            )
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

