package com.akhnaton.foodvisits.ui.home.visits2

import android.Manifest
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.databinding.FragmentGpsVisitBinding
import com.akhnaton.foodvisits.databinding.FragmentTelephoneVisitBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.openLocationInMap
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresPermission
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.data.model.saveVisitGps.Data
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.akhnaton.foodvisits.shared.getDistanceFromCurrentLocation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GpsVisitFragment : Fragment() {

    companion object {
        private const val TAG = "GpsVisitFragment"
    }

    private val viewModel: Visits2ViewModel by viewModels()
    private lateinit var binding: FragmentGpsVisitBinding
    private lateinit var dialog: AlertDialog

    lateinit var customerName: String
    lateinit var customerCode: String
    lateinit var siteAddress: String
    lateinit var customerPartySiteId: String
    lateinit var saleType: String
    var customerLatitude: Double? = 0.0
    var customerLongitude: Double? = 0.0
    var validGpsRange: Int? = 0
    var anotherOrderType: String = ""

    var promotersNotes: String = ""
    var grade: String = ""
    var visibility: String = "A"
    var comment: String = ""
    var phoneVisit: String = "0"
    var dateVisit: Long = 0
    var visitTarget: String = ""
    var actTarget: String = ""
    var checkIn: String = ""
    var orderType: String = "SALE"
    var customerType: String = "RETAIL"

    var isProm: Boolean = false

    private var timerHandler = Handler(Looper.getMainLooper())
    private var startTimeMillis = 0L

    val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTimeMillis
            val hours = elapsed / (1000 * 60 * 60)
            val minutes = (elapsed / (1000 * 60)) % 60
            val seconds = (elapsed / 1000) % 60
            binding.tvTimer.text = String.format(
                "%02d:%02d:%02d", hours, minutes, seconds
            )
            timerHandler.postDelayed(this, 1000)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentDistanceMeters: Float = 0f

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerName = arguments?.getString("customerName").toString()
        customerCode = arguments?.getString("customerCode").toString()
        siteAddress = arguments?.getString("siteAddress").toString()
        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()
        customerLatitude = arguments?.getDouble("customerLatitude")
        customerLongitude = arguments?.getDouble("customerLongitude")
        validGpsRange = arguments?.getInt("validGpsRange")

        isProm = SharedPreferencesHelper.getInstance().getProm()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        checkIn = startTimer()

        if (isProm) binding.llPromoterProcedures.visibility = View.VISIBLE
        else binding.llPromoterProcedures.visibility = View.GONE

        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                Visits2Intent.VisitsSelect(
                    saleType, customerCode
                )
            )
        }

        binding.btnOpenMap.setOnClickListener {
            openLocationInMap(
                requireContext(), customerLatitude!!, customerLongitude!!
            )
        }

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

                binding.tvDistance.text = "%.2f KM".format(currentDistanceMeters / 1000)

                Log.d(
                    "Distance", "%.2f KM".format(distanceKm)
                )
            }
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
            Log.d("WHATbtnSave", "Clicked")
//            checkInDate = getCurrentTimeTimestamp()
//            dateVisit = getCurrentDateTimestamp()
            actTarget = binding.etCollectToday.text.toString()
            comment = binding.etVisitNotes.text.toString()
            visitTarget = binding.etObjectiveVisit.text.toString()

            dateVisit = endTimer()

            lifecycleScope.launch {
                viewModel.visitsIntent.send(
                    Visits2Intent.SaveVisitGps(
                        SaveVisitGpsReq(
                            party_site_id = customerPartySiteId,
                            visit_target = visitTarget.toInt(),
                            ord_type = saleType,
                            visibility = visibility,
                            grade = grade,
                            act_target = actTarget.toInt(),
                            another_order_type = anotherOrderType,
                            comment = comment,
                            check_in = checkIn,
                            phone_visit = phoneVisit,
                            device_type = "Android version - ${BuildConfig.VERSION_NAME}",
                            latitude = customerLatitude.toString(),
                            longitude = customerLongitude.toString(),
                            zone_flag = if (currentDistanceMeters <= (validGpsRange ?: 0)) "IN"
                            else "OUT",
                        )
                    )
                )
            }
        }

//        getCustomerData()
        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is Visits2Status.Idle -> {}
                    is Visits2Status.Loading -> dialog.show()

                    is Visits2Status.SaveVisitGps -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
//                            val data =
//                                Gson().fromJson(
//                                    it.data.data,
//                                    Data::class.java
//                                )
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
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
                                        R.id.toOrderCreationCycle, bundle
                                    )
                                })
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
                                })
                        }
                    }

                    is Visits2Status.VisitsSelect -> {
                        dialog.dismiss()
                        if (it.data.status == 200) {
                            val visitGoal = it.data.data.visit_goal
                            val visabilty = it.data.data.visabilty
                            val sendOrderNote = it.data.data.send_order_note
//                            val positions = listOf(
//                                getString(R.string.order),
//                                getString(R.string.collection),
//                                getString(R.string.negative)
//                            )

                            val visitGoalStrings = visitGoal.map { it.name }
                            val visabiltyStrings = visabilty.map { it.name }
                            val sendOrderNoteStrings = sendOrderNote.map { it.name }

                            val adapter1 = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                visitGoalStrings
                            )
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
                                })
                        }
                    }

//                    is Visits2Status.GetCustomerData -> {
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

                    is Visits2Status.RefreshToken -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            Log.d("WHATRefreshToken", "${it.data.message}")
                            val data = Gson().fromJson(
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
                                    SharedPreferencesHelper.getInstance().logOut()
                                    startActivity(
                                        Intent(
                                            requireContext(), LoginActivity::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                })
                        }
//                        binding.tryAgainButtons.root.visibility = View.GONE

                    }

//                    is Visits2Status.Error -> {
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

    fun startTimer(): String {

        val currentMillis = System.currentTimeMillis()

        checkIn = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.ENGLISH
        ).format(Date(currentMillis))

        startTimeMillis = currentMillis

        timerHandler.post(timerRunnable)

        return checkIn
    }

    fun endTimer(): Long {
        dateVisit = System.currentTimeMillis() / 1000
        timerHandler.removeCallbacks(timerRunnable)
        return dateVisit
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_gps_visit, container, false
        )
        return binding.root
    }

}