package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.Data
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentPhoneVisits2Binding
import com.akhnaton.foodvisits.databinding.FragmentPhoneVisitsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails.OrderDetailsAdapter
import com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails.ReturnDetailsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PhoneVisitsFragment2 : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentPhoneVisits2Binding
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
    private var customerName: String = ""
    private var customerPartySiteId: String = ""
    private var limitArea: Int = 0
    private lateinit var dialog: AlertDialog
    private lateinit var progressBar: SweetAlertDialog
    private lateinit var mainLineAdapter: MainLineAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_phone_visits2,
                container,
                false
            )


        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

//        binding.addVisit.setOnClickListener(this)
//        binding.tryAgainButtons.tryAgain.setOnClickListener { getData() }

        getOrderTypeItemClick()
        getCustomerTypeItemClick()
        getLinesItemClick()
        getMainLineItemClick()
        getData()
        fetchData()

        return binding.root
    }

    private fun showDialog() {
        progressBar = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        progressBar.setTitleText("تنبيه!...")
            .setContentText("لا يمكنك انشاء زيارة هاتفية")
            .setConfirmText("OK")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                progressBar.dismiss()
            }
        progressBar.setCancelable(false)
        progressBar.show()
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.Idle -> dialog.show()
                    is PhoneVisitsStatus.Loading -> dialog.show()

                    is PhoneVisitsStatus.GetSalesAndCustomerTypes -> {
                        dialog.hide()
                        Log.d("WHAT", "${it.data.status}")
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    Data::class.java
                                )
                            setRecycler(data.sales_types.toMutableList())
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
//                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is PhoneVisitsStatus.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                            getData()
                        } else {

                        }
//                        binding.tryAgainButtons.root.visibility = View.GONE

                    }

                    is PhoneVisitsStatus.GetCustomerType -> {
                        dialog.hide()
                        customerType.clear()
                        (customerTypeList as ArrayList).clear()
                        val mCustomerList = it.data.data.user_customer_type
                        customerTypeList = mCustomerList
                        mCustomerList.forEach { customer ->
                            customerType.add(customer.customer_name)
                        }
//                        setAdapter(binding.customerType, customerType)
                    }


                    is PhoneVisitsStatus.GetLines -> {
                        dialog.hide()
                        linesName.clear()
//                        binding.lines.text.clear()
                        linesList = it.data.data.user_lines
                        linesList.forEach { line -> linesName.add(line.line_name) }
//                        setAdapter(binding.lines, linesName)
                    }


                    is PhoneVisitsStatus.GetCustomerLines -> {
                        dialog.hide()
                        mainList = it.data.data.main_customer_line

                        mainLineAdapter = MainLineAdapter(requireContext(), mainList)
//                        binding.mainLine.setAdapter(mainLineAdapter)


//                        binding.mainLine.setOnItemClickListener { _, _, position, _ ->
//                            val selectedItem = mainLineAdapter.getItem(position)
//                            binding.mainLine.postDelayed({
//                                binding.mainLine.showDropDown() }, 100)
//                            selectedItem?.let {
//                                handleMainLineSelection(it)
//
//
//                            }
//                        }


//                        binding.mainLine.addTextChangedListener(object : TextWatcher {
//                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//                            override fun afterTextChanged(s: Editable?) {
//                                mainLineAdapter.filter.filter(s)
//
//                                if (!binding.mainLine.isPopupShowing) {
//                                    binding.mainLine.showDropDown()
//                                }
//                            }
//                        })


//                        binding.mainLine.setOnClickListener {
//                            if (binding.mainLine.text.isNullOrEmpty()) {
//                                mainLineAdapter.filter.filter("")
//                            }
//                            binding.mainLine.post {
//                                binding.mainLine.showDropDown()
//                            }
//                        }


//                        binding.mainLine.post {
//                            binding.mainLine.showDropDown()
//                        }
                    }


                    is PhoneVisitsStatus.GetCustomersSite -> {
                        dialog.hide()
                        val mSitesList: MutableList<String> = ArrayList()
                        customerSiteList = it.data.data.customer_site

                        it.data.data.customer_site.forEach { sites -> mSitesList.add(sites.customer_name) }
                        getCustomersSiteSpinner(mSitesList)
                    }

//                    is PhoneVisitsStatus.GetAppSetting -> {
//                        dialog.hide()
//                        limitArea = it.data.data.limit_area
//                        Log.d(TAG, "LimitArea: ${it.data.data.limit_area}")
//                    }


                    is PhoneVisitsStatus.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dialog.hide()

//                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }


                    else -> {}
                }
            }
        }
    }

    private fun handleMainLineSelection(selectedItem: MainCustomerLine) {
//        binding.customerLayout.visibility = View.VISIBLE
//        binding.linesLayout.visibility = View.VISIBLE
//        binding.mainLineLayout.visibility = View.VISIBLE
//        binding.sitesLineLayout.visibility = View.VISIBLE
//        binding.addVisit.visibility = View.GONE
//        binding.sitesSpinner.setText("")


//        binding.mainLine.setText(selectedItem.customer_name)


        mainCustomerLinePosition = selectedItem.customer_code
        customerName = selectedItem.customer_name

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

    //Get Order Type
    private fun getOrderTypeItemClick() {
//        binding.orderType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
//            val selected = adapter?.getItemAtPosition(position)
//            orderType = selected.toString()
//            binding.customerLayout.visibility = View.VISIBLE
//            binding.linesLayout.visibility = View.GONE
//            binding.mainLineLayout.visibility = View.GONE
//            binding.sitesLineLayout.visibility = View.GONE
//            binding.addVisit.visibility = View.GONE
//
//            binding.customerType.setText("")
//            binding.lines.setText("")
//            binding.mainLine.setText("")
//            binding.sitesSpinner.setText("")
//
//            lifecycleScope.launch {
//                viewModel.phoneVisitsIntent.send(
//                    PhoneVisitsIntent.GetCustomerType(
//                        versionName,
//                        SharedPreferencesHelper.getInstance().getUserToken()
//                    )
//                )
//            }
//
//
//            Log.d(TAG, "getOrderTypeItemClick: $orderType")
//        }
    }

    //Get User Customer Type
    private fun getCustomerTypeItemClick() {
//        binding.customerType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
//            binding.customerLayout.visibility = View.VISIBLE
//            binding.linesLayout.visibility = View.VISIBLE
//            binding.mainLineLayout.visibility = View.GONE
//            binding.sitesLineLayout.visibility = View.GONE
//            binding.addVisit.visibility = View.GONE
//
//            binding.lines.setText("")
//            binding.mainLine.setText("")
//            binding.sitesSpinner.setText("")
//
//            customerTypePosition = customerTypeList[position].customer_type_id
//            lifecycleScope.launch {
//                viewModel.phoneVisitsIntent.send(
//                    PhoneVisitsIntent.GetLines(
//                        versionName,
//                        SharedPreferencesHelper.getInstance().getUserToken(),
//                        customerTypePosition, orderType
//                    )
//                )
//            }
//        }
    }

    //Get User Lines
    private fun getLinesItemClick() {
//        binding.lines.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
//            binding.customerLayout.visibility = View.VISIBLE
//            binding.linesLayout.visibility = View.VISIBLE
//            binding.mainLineLayout.visibility = View.VISIBLE
//            binding.sitesLineLayout.visibility = View.GONE
//            binding.addVisit.visibility = View.GONE
//
//            binding.mainLine.setText("")
//            binding.sitesSpinner.setText("")
//
//            customerLinePosition = linesList[position].line_id
//
//            lifecycleScope.launch {
//                viewModel.phoneVisitsIntent.send(
//                    PhoneVisitsIntent.GetCustomerLines(
//                        versionName,
//                        SharedPreferencesHelper.getInstance().getUserToken(),
//                        customerTypePosition, orderType, customerLinePosition
//                    )
//                )
//            }
//        }
    }

    //Get Main Customer Lines
    private fun getMainLineItemClick() {
//        binding.mainLine.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
//            binding.customerLayout.visibility = View.VISIBLE
//            binding.linesLayout.visibility = View.VISIBLE
//            binding.mainLineLayout.visibility = View.VISIBLE
//            binding.sitesLineLayout.visibility = View.VISIBLE
//            binding.addVisit.visibility = View.GONE
//
//            binding.sitesSpinner.setText("")
//
//            mainCustomerLinePosition = mainList[position].customer_code
//            customerName = mainList[position].customer_name
//
//            lifecycleScope.launch {
//                viewModel.phoneVisitsIntent.send(
//                    PhoneVisitsIntent.GetCustomersSite(
//                        version = versionName,
//                        token = SharedPreferencesHelper.getInstance().getUserToken(),
//                        customerType = customerTypePosition,
//                        orderType = orderType,
//                        lineId = customerLinePosition,
//                        customerCode = mainCustomerLinePosition
//                    )
//                )
//            }
//        }
    }

    private fun getCustomersSiteSpinner(mList: MutableList<String>) {
//        binding.sitesSpinner.setItems(mList.toTypedArray())
//        binding.sitesSpinner.setTitle("Choose Customer Site")
//        binding.sitesSpinner.setExpandTint(R.color.colorAccent)

//        binding.sitesSpinner.setOnItemClickListener {
//            customerPartySiteId = customerSiteList[it].customer_name
//            mListPassedData = customerSiteList[it]
//            binding.customerLayout.visibility = View.VISIBLE
//            binding.linesLayout.visibility = View.VISIBLE
//            binding.mainLineLayout.visibility = View.VISIBLE
//            binding.sitesLineLayout.visibility = View.VISIBLE
//            binding.addVisit.visibility = View.VISIBLE
//        }
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

    //Set default adapter
    private fun setRecycler(
        list: MutableList<String>
    ) {

        val adapter =
            Card1Adapter(
                object : Card1Adapter.OnItemClickListener {

                    override fun onClick(item: String) {

                        Log.d("WHATclick", "HII CLICK")

                        val bundle = Bundle().apply {
                            putString("saleType", item)
                        }

                        findNavController().navigate(
                            R.id.toCustomers,
                            bundle
                        )
//                        val intent = Intent(
//                            requireContext(),
//                            CustomersActivity::class.java
//                        )
//
//                        intent.putExtra(
//                            "CARD_NAME",
//                            item
//                        )
//
//                        startActivity(intent)
                    }
                }
            )

        adapter.setList(list)

        binding.rv.layoutManager =
            GridLayoutManager(
                requireContext(),
                2
            )

        binding.rv.adapter = adapter
        binding.rv.itemAnimator = DefaultItemAnimator()
    }

    private fun getData() {
        lifecycleScope.launch {
//            viewModel.phoneVisitsIntent.send(
//                PhoneVisitsIntent.GetPlan(
//                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
//                )
//            )

            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetSalesAndCustomerTypes
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
                Intent(requireActivity(), PhoneVisitsDetailsActivity::class.java)
                    .putExtra("customerPartySiteId", customerPartySiteId)
                    .putExtra("time", tsLong.toString())
                    .putExtra("customerSiteData", mListPassedData)
                    .putExtra("customer_code", mainCustomerLinePosition)
                    .putExtra("orderType", orderType)
                    .putExtra("customerTypePosition", customerTypePosition)
                    .putExtra("customer_name", customerName)


            )
            Log.d("DEBUG_DATA", "Name: ${customerName}")
            Log.d("DEBUG_DATA", "Address: ${mainCustomerLinePosition}")

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

