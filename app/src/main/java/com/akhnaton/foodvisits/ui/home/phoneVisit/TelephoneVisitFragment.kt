package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getCustomerData.Data
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentCustomerDetailsBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.BaseActivity
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    var hours: Long = 0
    var minutes: Long = 0
    var seconds: Long = 0

    var anotherOrderType: String = ""
    var grade: String = ""
    var visibility: String = "A"
    var comment: String = ""
    var phoneVisit: String = "1"
    var dateVisit: Long = 0
    var visitTarget: String = ""
    var actTarget: String = ""
    var checkIn: String = ""
    var currentTime: String = ""

    var orderType: String = "SALE"
    var customerType: String = "RETAIL"

    var isProm: Boolean = false

    private var timerHandler = Handler(Looper.getMainLooper())
    private var startTimeMillis = 0L

    var timerRunnable = object : Runnable {
        override fun run() {
            val elapsed =
                System.currentTimeMillis() - startTimeMillis
            val hours =
                elapsed / (1000 * 60 * 60)
            val minutes =
                (elapsed / (1000 * 60)) % 60
            val seconds =
                (elapsed / 1000) % 60
            binding.tvTimer.text =
                String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
                )
            timerHandler.postDelayed(this, 1000)
        }
    }

    private var checkInTimeMillis = 0L
    private var apiCurrentTimeMillis = 0L

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
        checkIn =
            arguments?.getString("checkIn").toString()
        currentTime =
            arguments?.getString("currentTime").toString()

        if (!checkIn.isNullOrBlank() && !currentTime.isNullOrBlank()) {

            checkInTimeMillis = parseApiDate(checkIn)
            apiCurrentTimeMillis = parseApiDate(currentTime)

            startTimer()
        }

//        hours =
//            arguments?.getLong("hours")!!
//        minutes =
//            arguments?.getLong("minutes")!!
//        seconds =
//            arguments?.getLong("seconds")!!

        timerRunnable = object : Runnable {
            override fun run() {
                binding.tvTimer.text =
                    String.format(
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                    )
                timerHandler.postDelayed(this, 1000)
            }
        }

        isProm = SharedPreferencesHelper.getInstance().getProm()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        MainActivity.binding.navView2.visibility = View.GONE

        if (isProm) binding.llPromoterProcedures.visibility = View.VISIBLE
        else binding.llPromoterProcedures.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.VisitsSelect(
                    saleType, customerCode
                )
            )
        }

        binding.cardVisitReport.setOnClickListener {

        }

        binding.cardCalls.setOnClickListener {

        }

        binding.cardImages.setOnClickListener {

        }

        binding.cardInventory.setOnClickListener {

        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvCustomerName.setText(customerName)
        binding.tvCustomerCode.setText(customerCode)
        binding.tvSiteAddress.setText(siteAddress)

//        val positions = listOf(
//            getString(R.string.order),
//            getString(R.string.collection),
//            getString(R.string.negative)
//        )
//
//        val adapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_dropdown_item_1line,
//            positions
//        )
//
//        binding.etVisitingPosition.setAdapter(adapter)
//
//        binding.etVisitingPosition.setOnItemClickListener { _, _, position, _ ->
//            val selectedPosition = positions[position]
//            if (selectedPosition == getString(R.string.order)) grade = "A"
//            else if (selectedPosition == getString(R.string.collection)) grade = "B"
//            else if (selectedPosition == getString(R.string.negative)) grade = "C"
//        }

        binding.btnSave.setOnClickListener {
            if (binding.etObjectiveVisit.text.toString().isEmpty()) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "هدف الزيارة مطلوب",
                    isSuccess = false,
                    showOkButton = true
                )
                return@setOnClickListener
            }
            if (binding.etVisitingPosition.text.toString().isEmpty()) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "موقف الزيارة مطلوب",
                    isSuccess = false,
                    showOkButton = true
                )
                return@setOnClickListener
            }
            Log.d("WHATbtnSave", "Clicked")
//            checkIn = getCurrentTimeTimestamp().toString()
//            dateVisit = getCurrentDateTimestamp()
            actTarget = binding.etCollectToday.text.toString()
            comment = binding.etVisitNotes.text.toString()
            visitTarget = binding.etObjectiveVisit.text.toString()

            dateVisit = endTimer()

            lifecycleScope.launch {
                viewModel.phoneVisitsIntent.send(
                    PhoneVisitsIntent.SaveVisitPhone(
                        SaveVisitPhoneReq(
                            party_site_id = customerPartySiteId,
                            visit_target = visitTarget.toInt(),
                            ord_type = saleType,
                            visibility = visibility,
                            grade = grade,
                            act_target = if (actTarget.isNotEmpty()) actTarget.toInt() else 0,
//                            act_target = if (actTarget.isEmpty()),
                            another_order_type = anotherOrderType,
                            comment = comment,
                            check_in = checkIn,
                            phone_visit = phoneVisit,
                            device_type = "Android"

//                            date_visit = dateVisit,
//                            promoters_notes = promotersNotes,
//                            visit_notes = visitNotes,
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

    private fun parseApiDate(date: String): Long {
        val format = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.getDefault()
        )

        return format.parse(date)?.time ?: 0L
    }

    private fun startTimer() {

        // Initial difference calculated from the API
        var elapsed = apiCurrentTimeMillis - checkInTimeMillis

        timerRunnable = object : Runnable {

            override fun run() {

                val hours = elapsed / (1000 * 60 * 60)
                val minutes = (elapsed / (1000 * 60)) % 60
                val seconds = (elapsed / 1000) % 60

                binding.tvTimer.text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
                )

                // Add exactly 1 second for the next update
                elapsed += 1000

                timerHandler.postDelayed(this, 1000)
            }
        }

        timerHandler.post(timerRunnable)
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.Idle -> {}
                    is PhoneVisitsStatus.Loading -> dialog.show()

                    is PhoneVisitsStatus.SaveVisitPhone -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.saveVisitPhone.Data::class.java
                                )
                            //VISITS_APK
                            var message = "${it.data.message}"
//                            DialogUtils.showResultDialog(
//                                context = requireContext(),
//                                message = message,
//                                isSuccess = true,
//                                showOkButton = true,
//                                onOk = {
//                                    MainActivity.binding.navView2.visibility = View.VISIBLE
//                                    findNavController().navigate(
//                                        R.id.toHome
//                                    )
//                                }
//                            )
                            if (!SharedPreferencesHelper.getInstance().isAllowedToMakeOrder()) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = "غير مسموح لك بعمل طلبيات , الرجاء التواصل مع الإدارة المالية",
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                        MainActivity.binding.navView2.visibility = View.VISIBLE
                                        findNavController().navigate(
                                            R.id.toHome
                                        )
                                    }
                                )
                                return@collect
                            }
                            if (data.is_suspended == true) {
                                message = "${data.message}"
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = message,
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                        MainActivity.binding.navView2.visibility = View.VISIBLE
                                        findNavController().navigate(
                                            R.id.toHome
                                        )
                                    }
                                )
                            } else if (data.is_suspended == false) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = message,
                                    isSuccess = true,
                                    seconds = 2,
                                    onAutoDismiss = {
                                        if (grade == "A") {
                                            val bundle = Bundle().apply {
                                                putString("customerName", customerName)
                                                putString("customerCode", customerCode)
                                                putString("siteAddress", siteAddress)
                                                putString(
                                                    "customerPartySiteId",
                                                    customerPartySiteId
                                                )
                                                putString("saleType", saleType)
                                                putString("fragment", "Telephone")
                                            }

                                            findNavController().navigate(
                                                R.id.toOrderCreationCycle,
                                                bundle
                                            )
                                        } else {
                                            MainActivity.binding.navView2.visibility = View.VISIBLE
                                            findNavController().navigate(
                                                R.id.toHome
                                            )
                                        }
                                    }
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

                    is PhoneVisitsStatus.VisitsSelect -> {
                        dialog.dismiss()
                        binding.tvTimer.visibility = View.VISIBLE
                        if (it.data.status == 200) {
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.visitesSelect.Data::class.java
                                )
                            val visitGoal = data.visit_goal
                            val visabilty = data.visabilty
                            val sendOrderNote = data.send_order_note

                            val visitGoalStrings = visitGoal.map { it.name }
                            val visabiltyStrings = visabilty.map { it.name }
                            val sendOrderNoteStrings = sendOrderNote.map { it.name }

                            val adapter1 = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                visitGoalStrings
                            )
                            if (visitGoal.size > 0) {
                                grade = visitGoal[0].id
                                binding.etVisitingPosition.setText(visitGoal[0].name)
                            }
                            binding.etVisitingPosition.setAdapter(adapter1)
                            binding.etVisitingPosition.setOnItemClickListener { _, _, position, _ ->
                                val selectedPosition = visitGoal[position]
                                grade = selectedPosition.id
                            }

                            val adapter2 = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                visabiltyStrings
                            )
                            binding.etVisibility.setAdapter(adapter2)
                            binding.etVisibility.setOnItemClickListener { _, _, position, _ ->
                                val selectedPosition = visabilty[position]
                                visibility = selectedPosition.id
                                Log.d("WHATvisibility", "$visibility")
                            }

                            val adapter3 = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                sendOrderNoteStrings
                            )
                            binding.etSendOrderNote.setAdapter(adapter3)
                            binding.etSendOrderNote.setOnItemClickListener { _, _, position, _ ->
                                val selectedPosition = sendOrderNote[position]
                                anotherOrderType = selectedPosition.id
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

                    is PhoneVisitsStatus.Error -> {
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

//    fun startTimer(): String {
//
//        val currentMillis = System.currentTimeMillis()
//
//        checkIn = SimpleDateFormat(
//            "dd-MM-yyyy HH:mm:ss",
//            Locale.ENGLISH
//        ).format(Date(currentMillis))
//
//        startTimeMillis = currentMillis
//
//        timerHandler.post(timerRunnable)
//
//        return checkIn
//    }

    fun endTimer(): Long {
        dateVisit =
            System.currentTimeMillis() / 1000
        timerHandler.removeCallbacks(timerRunnable)
        return dateVisit
    }

    override fun onDestroyView() {
        super.onDestroyView()

        timerHandler.removeCallbacks(timerRunnable)
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