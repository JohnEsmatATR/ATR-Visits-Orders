package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.Manifest
import android.app.ProgressDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentPhoneVisitsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch

class PhoneVisitsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentPhoneVisitsBinding
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var mListPassedData: SitesData
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
    private var limitArea: Int = 0
    private lateinit var dialog: ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_phone_visits,
                container,
                false
            )



        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        binding.addVisit.setOnClickListener(this)
        binding.tryAgainButtons.tryAgain.setOnClickListener { getData() }

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
                    is PhoneVisitsStatus.Idle -> dialog.show()
                    is PhoneVisitsStatus.Loading -> dialog.show()

                    //Get Order Type
                    is PhoneVisitsStatus.Plan -> {
                        dialog.hide()
                        setAdapter(binding.orderType, it.data.data.user_order_type.toMutableList())
                        binding.tryAgainButtons.root.visibility = View.GONE
                        binding.orderTypeLayout.visibility = View.VISIBLE

                    }

                    //Get User Customer Type
                    is PhoneVisitsStatus.GetCustomerType -> {
                        dialog.hide()
                        val mCustomerList = it.data.data.user_customer_type
                        customerTypeList = mCustomerList
                        mCustomerList.forEach { customer ->
                            customerType.add(customer.customer_name)
                        }
                        setAdapter(binding.customerType, customerType)
                    }

                    //Get User Lines
                    is PhoneVisitsStatus.GetLines -> {
                        dialog.hide()
                        linesName.clear()
                        binding.lines.text.clear()
                        linesList = it.data.data.user_lines
                        linesList.forEach { line -> linesName.add(line.line_name) }
                        setAdapter(binding.lines, linesName)
                    }

                    //Get Main Customer Lines
                    is PhoneVisitsStatus.GetCustomerLines -> {
                        dialog.hide()
                        mainLine.clear()
                        binding.mainLine.text.clear()
                        mainList = it.data.data.main_customer_line
                        it.data.data.main_customer_line.forEach { line -> mainLine.add(line.customer_name) }
                        setAdapter(binding.mainLine, mainLine)
                    }

                    is PhoneVisitsStatus.GetCustomersSite -> {
                        dialog.hide()
                        val mSitesList: MutableList<String> = ArrayList()
                        customerSiteList = it.data.data.customer_site

                        it.data.data.customer_site.forEach { sites -> mSitesList.add(sites.customer_name) }
                        getCustomersSiteSpinner(mSitesList)
                    }

                    is PhoneVisitsStatus.GetAppSetting -> {
                        dialog.hide()
                        limitArea = it.data.data.limit_area
                        Log.d(TAG, "LimitArea: ${it.data.data.limit_area}")
                    }

                    //Catch Error
                    is PhoneVisitsStatus.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()
                        binding.orderTypeLayout.visibility = View.GONE
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }


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
                viewModel.phoneVisitsIntent.send(
                    PhoneVisitsIntent.GetLines(
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
                viewModel.phoneVisitsIntent.send(
                    PhoneVisitsIntent.GetCustomerLines(
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
                viewModel.phoneVisitsIntent.send(
                    PhoneVisitsIntent.GetCustomersSite(
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
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )

            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomerType(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )

            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetAppSetting(
                    versionName
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
                    PhoneVisitsDetailsActivity::class.java
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

