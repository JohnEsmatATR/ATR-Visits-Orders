package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import calculateTimeDifference
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.checkInPhone.CheckInPhoneReq
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getSalesAndCustomerTypes.Data
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentPhoneVisitStepsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.getValue

class PhoneVisitStepsFragment : Fragment() {

    companion object {
        private const val TAG = "PhoneVisitStepsFragment"
    }

    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: FragmentPhoneVisitStepsBinding
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var mListPassedData: SitesData
    private var orderType: String = ""
    private val customerType: MutableList<String> = ArrayList()
    private val linesName: MutableList<String> = ArrayList()
    private var customerTypeList: List<CustomerType> = ArrayList()
    private var linesList: List<LinesUsers> = ArrayList()
    private var mainList: List<MainCustomerLine> = ArrayList()
    private var customerSiteList: List<SitesData> = ArrayList()
    private var customerTypePosition: String = ""
    private var mainCustomerLinePosition: String = ""
    private lateinit var dialog: AlertDialog
    private lateinit var mainLineAdapter: MainLineAdapter

    private var saleType: String = ""
    private var customerCode: String = ""
    private var line: String = ""
    private var customerName: String = ""
    private var siteAddress: String = ""
    private var customerPartySiteId: String = ""

    private var allCustomers =
        mutableListOf<com.akhnaton.foodvisits.data.model.customers.Data>()

    private var displayedCustomers =
        mutableListOf<com.akhnaton.foodvisits.data.model.customers.Data>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        MainActivity.binding.navView2.visibility = View.VISIBLE

        binding.tvEdit1.setOnClickListener {
            binding.card1Selected.visibility = View.GONE
            binding.card1NotSelected.visibility = View.VISIBLE
            binding.card2NotSelected.visibility = View.GONE
            binding.card2Selected.visibility = View.GONE
            binding.card3NotSelected.visibility = View.GONE
            binding.btnStartVisit.visibility = View.GONE
        }

        binding.tvEdit2.setOnClickListener {
            binding.card2Selected.visibility = View.GONE
            binding.card2NotSelected.visibility = View.VISIBLE
            binding.card3NotSelected.visibility = View.GONE
            binding.btnStartVisit.visibility = View.GONE
        }

        binding.etSearch.addTextChangedListener {
            filterCustomers(
                it.toString().trim()
            )
        }

        binding.btnStartVisit.setOnClickListener {
            Log.d("WHAT", "Triggered0")
            checkInZero(customerPartySiteId, saleType)
        }

        getData()
        fetchData()

    }

    private fun checkInZero(
        customerPartySiteId: String,
        saleType: String
    ) {
        val checkIn = CheckInPhoneReq(
            insert = 0,
            ord_type = saleType,
            party_site_id = customerPartySiteId,
            phone_visit = "1"
        )
        Log.d("WHATcheckIn", checkIn.toString())
        Log.d("WHAT", "Triggered1")
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.CheckIn(
                    checkIn
                )
            )
        }
    }

    private fun checkInOne(
        customerPartySiteId: String,
        saleType: String
    ) {
        val checkIn = CheckInPhoneReq(
            insert = 1,
            ord_type = saleType,
            party_site_id = customerPartySiteId,
            phone_visit = "1"
        )
        Log.d("WHATcheckIn", checkIn.toString())
        Log.d("WHAT", "Triggered1")
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.CheckIn(
                    checkIn
                )
            )
        }
    }

    private fun navigateToPhoneVisit(
        customerName: String,
        customerCode: String,
        siteAddress: String,
        customerPartySiteId: String,
        saleType: String,
        checkIn: String,
        currentTime: String
    ) {
        val navController = findNavController()

        // Prevent navigating from TelephoneVisitFragment
        if (navController.currentDestination?.id != R.id.visitPhoneFragment) {
            Log.e(
                "NAVIGATION",
                "Navigation ignored. Current destination = " +
                        "${navController.currentDestination?.label}"
            )
            return
        }

        val result = calculateTimeDifference(
            checkIn,
            currentTime
        )

        val bundle = Bundle().apply {
            putString("customerName", customerName)
            putString("customerCode", customerCode)
            putString("siteAddress", siteAddress)
            putString("customerPartySiteId", customerPartySiteId)
            putString("saleType", saleType)
            putString("checkIn", checkIn)
            putString("currentTime", currentTime)

            // Keep these if you still need them
            putLong("hours", result.hours)
            putLong("minutes", result.minutes)
            putLong("seconds", result.seconds)
        }

        Log.d(
            "NAVIGATION",
            "Navigating to TelephoneVisitFragment"
        )

        navController.navigate(
            R.id.toTelephoneVisit,
            bundle
        )
    }

    private fun getCustomers() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomers(
                    saleType
                )
            )
        }
    }

    private fun getCustomerData() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetCustomerData(
                    saleType,
                    customerCode,
                    line
                )
            )
        }
    }

    private fun filterCustomers(
        keyword: String
    ) {
        displayedCustomers =
            if (keyword.isBlank()) {
                allCustomers.toMutableList()
            } else {
                allCustomers.filter {
                    it.CATEGORY_CODE.contains(
                        keyword,
                        true
                    ) ||
                            it.DISPLAY_NAME.contains(
                                keyword,
                                true
                            ) ||
                            it.CUSTOMER_CODE.contains(
                                keyword,
                                true
                            ) ||
                            it.CATEGORY_MEANING.contains(
                                keyword,
                                true
                            ) ||
                            it.TEAM_NAME.contains(
                                keyword,
                                true
                            ) ||
                            it.CUSTOMER_PROFILE_DESC.contains(
                                keyword,
                                true
                            )
                }.toMutableList()
            }
        if (displayedCustomers.isEmpty()) {
            binding.llZeroState.visibility =
                View.VISIBLE
            binding.rv2.visibility =
                View.GONE
        } else {
            binding.llZeroState.visibility =
                View.GONE
            binding.rv2.visibility =
                View.VISIBLE
            setRecycler2(displayedCustomers)
        }
    }

    private fun getData() {
        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetSalesAndCustomerTypes
            )

//            viewModel.phoneVisitsIntent.send(
//                PhoneVisitsIntent.GetAppSetting(
//                    versionName
//                )
//            )
        }
    }

    private fun setRecycler1(
        list: MutableList<String>
    ) {

        val adapter =
            Card3Adapter(
                object : Card3Adapter.OnItemClickListener {

                    override fun onClick(item: String) {

                        Log.d("WHATclick", "HII CLICK")

                        saleType = item
                        binding.card1NotSelected.visibility = View.GONE
                        binding.card1Selected.visibility = View.VISIBLE
                        binding.chosenTypeSale.text = saleType
                        binding.card2NotSelected.visibility = View.VISIBLE
                        getCustomers()

//                        val bundle = Bundle().apply {
//                            putString("saleType", item)
//                        }
//
//                        findNavController().navigate(
//                            R.id.toTelephoneVisit,
//                            bundle
//                        )
                    }
                }
            )

        adapter.setList(list)

        binding.rv1.layoutManager =
            GridLayoutManager(
                requireContext(),
                2
            )

        binding.rv1.adapter = adapter
        binding.rv1.itemAnimator = DefaultItemAnimator()
    }

    private fun setRecycler2(
        list: MutableList<com.akhnaton.foodvisits.data.model.customers.Data>
    ) {
        val adapter = CustomersAdapter(list, { customer ->

            customerCode = customer.CUSTOMER_CODE
            line = customer.TEAM_NAME
            customerName = customer.CUSTOMER_NAME

            binding.card2NotSelected.visibility = View.GONE
            binding.card2Selected.visibility = View.VISIBLE
            binding.tvChosenCustomer.text = customerName
            binding.card3NotSelected.visibility = View.VISIBLE
            getCustomerData()
//            val bundle = Bundle().apply {
//                putString("saleType", saleType)
//                putString("customerCode", customer.CUSTOMER_CODE)
//                putString("line", customer.TEAM_NAME)
//                putString("customerName", customer.CUSTOMER_NAME)
//            }

            binding.etSearch.text?.clear()

//            findNavController().navigate(
//                R.id.toCustomerDetails,
//                bundle
//            )
        })

        binding.rv2.layoutManager =
            LinearLayoutManager(
                requireContext(),
            )

        binding.rv2.adapter = adapter
        binding.rv2.itemAnimator = DefaultItemAnimator()
    }

    private fun setRecycler3(
        list: MutableList<CustomerAddres>
    ) {
        val adapter =
            CustomerDataAdapter(
                object : CustomerDataAdapter.OnItemClickListener {

                    override fun onClick(item: CustomerAddres) {

                        Log.d("WHATclick", "HII CLICK")

                        binding.btnStartVisit.visibility = View.VISIBLE

                        siteAddress = item.SITE_ADDRESS
                        customerPartySiteId = item.PARTY_SITE_ID

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

        binding.rv3.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rv3.adapter = adapter
        binding.rv3.itemAnimator = DefaultItemAnimator()
    }

//    override fun onClick(v: View?) {
//        val tsLong = System.currentTimeMillis() / 1000
//
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) ==
//            PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) ==
//            PackageManager.PERMISSION_GRANTED
//        ) {
//            startActivity(
//                Intent(requireActivity(), PhoneVisitsDetailsActivity::class.java)
//                    .putExtra("customerPartySiteId", customerPartySiteId)
//                    .putExtra("time", tsLong.toString())
//                    .putExtra("customerSiteData", mListPassedData)
//                    .putExtra("customer_code", mainCustomerLinePosition)
//                    .putExtra("orderType", orderType)
//                    .putExtra("customerTypePosition", customerTypePosition)
//                    .putExtra("customer_name", customerName)
//
//
//            )
//            Log.d("DEBUG_DATA", "Name: ${customerName}")
//            Log.d("DEBUG_DATA", "Address: ${mainCustomerLinePosition}")
//
//        } else {
//            if (ContextCompat.checkSelfPermission(
//                    requireActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) !=
//                PackageManager.PERMISSION_GRANTED
//            ) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        requireActivity(),
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    )
//                ) {
//                    ActivityCompat.requestPermissions(
//                        requireActivity(),
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
//                    )
//                } else {
//                    ActivityCompat.requestPermissions(
//                        requireActivity(),
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
//                    )
//                }
//            }
//        }
//
//    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
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
                                setRecycler1(data.sales_types.toMutableList())
                            } else if (it.data.status == 401) {
                                lifecycleScope.launch {
                                    viewModel.phoneVisitsIntent.send(
                                        PhoneVisitsIntent.RefreshToken(
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
                                    }
                                )
                            }
                        }

                        is PhoneVisitsStatus.GetCustomers -> {
                            dialog.dismiss()
                            if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    Data::class.java
//                                )
                                allCustomers.clear()
                                allCustomers =
                                    it.data.data.toMutableList()
                                displayedCustomers =
                                    allCustomers.toMutableList()
                                if (displayedCustomers.isEmpty()) {
                                    binding.llZeroState.visibility =
                                        View.VISIBLE
                                    binding.rv2.visibility =
                                        View.GONE
                                } else {
                                    binding.llZeroState.visibility =
                                        View.GONE
                                    binding.rv2.visibility =
                                        View.VISIBLE
                                    setRecycler2(displayedCustomers)
                                }

                            } else if (it.data.status == 401) {
                                lifecycleScope.launch {
                                    viewModel.phoneVisitsIntent.send(
                                        PhoneVisitsIntent.RefreshToken(
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

                        is PhoneVisitsStatus.GetCustomerData -> {
                            dialog.dismiss()
                            if (it.data.status == 200) {
                                val data =
                                    Gson().fromJson(
                                        it.data.data,
                                        com.akhnaton.foodvisits.data.model.getCustomerData.Data::class.java
                                    )
                                setRecycler3(data.customer_address.toMutableList())
                            } else if (it.data.status == 401) {
                                lifecycleScope.launch {
                                    viewModel.phoneVisitsIntent.send(
                                        PhoneVisitsIntent.RefreshToken(
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

                        is PhoneVisitsStatus.CheckIn -> {
                            dialog.dismiss()
                            if (it.data.status == 200) {
                                val data =
                                    Gson().fromJson(
                                        it.data.data,
                                        com.akhnaton.foodvisits.data.model.checkInPhone.Data::class.java
                                    )
//                            Log.d("WHAT", "onClick: $clickedVisit")


                                if (data.visit_id != null) {
                                    navigateToPhoneVisit(
                                        customerName,
                                        customerCode,
                                        siteAddress,
                                        customerPartySiteId,
                                        saleType,
                                        data.check_in.toString(),
                                        data.current_time
                                    )
                                } else {
                                    Log.d("WHATbtnStartVisit", customerName)
                                    Log.d("WHATbtnStartVisit", customerCode)
                                    Log.d("WHATbtnStartVisit", siteAddress)
                                    Log.d("WHATbtnStartVisit", customerPartySiteId)
                                    Log.d("WHATbtnStartVisit", saleType)
                                    Log.d("WHAT", "${data.already_started}")
                                    Log.d("WHAT", "${data.visit_id}")
                                    Log.d("WHAT", "Triggered2")
                                    DialogUtils.showResultDialog(
                                        context = requireContext(),
                                        message = "هل تريد بدء الزيارة ؟",
                                        description = "سيبدأ حساب مدة المكالمة الآن مع ${customerName} ${siteAddress}",
                                        isSuccess = true,
                                        isStartVisit = true,
                                        onStartVisit = {
                                            checkInOne(customerPartySiteId, saleType)
                                        },
                                    )
                                }

                            } else if (it.data.status == 401) {
                                lifecycleScope.launch {
                                    viewModel.phoneVisitsIntent.send(
                                        PhoneVisitsIntent.RefreshToken(
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

                        is PhoneVisitsStatus.RefreshToken -> {
                            dialog.hide()
                            if (it.data.status == 200) {
                                Log.d("WHATRefreshToken", "${it.data.message}")
                                val data =
                                    Gson().fromJson(
                                        it.data.data,
                                        com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                    )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                                getData()
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
                        }


                        is PhoneVisitsStatus.GetLines -> {
                            dialog.hide()
                            linesName.clear()
                            linesList = it.data.data.user_lines
                            linesList.forEach { line -> linesName.add(line.line_name) }
                        }


                        is PhoneVisitsStatus.GetCustomerLines -> {
                            dialog.hide()
                            mainList = it.data.data.main_customer_line

                            mainLineAdapter = MainLineAdapter(requireContext(), mainList)
                        }


                        is PhoneVisitsStatus.GetCustomersSite -> {
                            dialog.hide()
                            val mSitesList: MutableList<String> = ArrayList()
                            customerSiteList = it.data.data.customer_site

                            it.data.data.customer_site.forEach { sites -> mSitesList.add(sites.customer_name) }
                        }

                        is PhoneVisitsStatus.Error -> {
                            Log.d(TAG, "fetchData: ${it.error}")
                            dialog.hide()
                        }

                        else -> {}
                    }
                }
            }
        }
//        lifecycleScope.launch {
//
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_phone_visit_steps,
                container,
                false
            )
        return binding.root
    }
}