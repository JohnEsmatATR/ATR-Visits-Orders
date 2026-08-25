package com.akhnaton.foodvisits.ui.home.visits2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
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
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.data.interfaces.location.ILocationClient
import com.akhnaton.foodvisits.data.model.saveVisitGps.Data
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.shared.LocationService.Companion.startService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.akhnaton.foodvisits.shared.getDistanceFromCurrentLocation
import com.akhnaton.foodvisits.shared.location.DefaultLocationClient
import com.akhnaton.foodvisits.shared.location.GetLocationService
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsDetailsActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModelFactory
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.REQUEST_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.toDoubleOrNull

class GpsVisitFragment : Fragment() {

    companion object {
        private const val TAG = "GpsVisitFragment"
    }

    //    private val viewModel: Visits2ViewModel by viewModels()
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
    lateinit var visitWithUserId: String
    lateinit var visitWithName: String
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
    var currentTime: String = ""

    var orderType: String = "SALE"
    var customerType: String = "RETAIL"

    var isProm: Boolean = false

    private var timerHandler = Handler(Looper.getMainLooper())
    private var startTimeMillis = 0L

    private var selectedRating = 0f

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

//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private var currentDistanceMeters: Float = 0f

    private var isDeveloperModeEnable = 0

    var hours: Long = 0
    var minutes: Long = 0
    var seconds: Long = 0

    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private lateinit var checkConnection: CheckConnection
    private lateinit var viewModel: Visits2ViewModel

    //    val customerLocation = Location("")
    private lateinit var locationClient: ILocationClient
//    val myLocation = Location("")

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboardInsets()

        customerName = arguments?.getString("customerName").toString()
        customerCode = arguments?.getString("customerCode").toString()
        siteAddress = arguments?.getString("siteAddress").toString()
        customerPartySiteId = arguments?.getString("customerPartySiteId").toString()
        saleType = arguments?.getString("saleType").toString()
        customerLatitude = arguments?.getDouble("customerLatitude")
        customerLongitude = arguments?.getDouble("customerLongitude")
        validGpsRange = arguments?.getInt("validGpsRange")
        visitWithUserId = arguments?.getString("visitWithUserId").toString()
        visitWithName = arguments?.getString("visitWithName").toString()
        checkIn =
            arguments?.getString("checkIn").toString()
        currentTime =
            arguments?.getString("currentTime").toString()

        viewModel = ViewModelProvider(
            this,
            Visits2ViewModelFactory(requireContext())
        )[Visits2ViewModel::class.java]
        checkConnection = CheckConnection(requireContext())

        askPermission()
        observeDistance()

//        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//        locationClient = DefaultLocationClient(
//            requireContext(),
//            LocationServices.getFusedLocationProviderClient(requireActivity())
//        )
//
//        locationClient
//            .getLocationUpdates(10000L)
//            .catch { e -> e.printStackTrace() }
//            .onEach { location ->
//                val lat = location.latitude.toString().takeLast(3)
//                val long = location.longitude.toString().takeLast(3)
//
//                calculateDistance(
//                    lat.toDouble(),
//                    long.toDouble(),
//                    customerLatitude,
//                    customerLongitude
//                )
//            }
//            .launchIn(serviceScope)

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                when (result.resultCode) {
                    locationPermissionCode -> when (result.resultCode) {
                        RESULT_OK -> Log.d("abc", "OK")
                        RESULT_CANCELED -> RequestPermission().enableLocation(requireActivity())
                    }
                }
            }
        }

        if (!checkIn.isNullOrBlank() && !currentTime.isNullOrBlank()) {

            checkInTimeMillis = parseApiDate(checkIn)
            apiCurrentTimeMillis = parseApiDate(currentTime)

            startTimer()
        }

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

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (isProm) {
            binding.llPromoterProcedures.visibility = View.VISIBLE
            binding.cardReport.visibility = View.GONE
        }
        else {
            binding.llPromoterProcedures.visibility = View.GONE
            binding.cardReport.visibility = View.VISIBLE
        }

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

//        getCurrentLocation()
//        observeDistance()

        binding.cardVisitReport.setOnClickListener {
            findNavController().navigate(
                R.id.toCompetitors
            )
        }

        binding.cardCalls.setOnClickListener {
            findNavController().navigate(
                R.id.toCalls
            )
        }

        binding.cardImages.setOnClickListener {
            val bundle = Bundle().apply {
                putString("customerCode", customerCode)
                putString(
                    "customerPartySiteId",
                    customerPartySiteId
                )
            }

            findNavController().navigate(
                R.id.toImages, bundle
            )
        }

        binding.cardInventory.setOnClickListener {
            val bundle = Bundle().apply {
                putString("customerCode", customerCode)
                putString(
                    "customerPartySiteId",
                    customerPartySiteId
                )
            }

            findNavController().navigate(
                R.id.toInventory, bundle
            )
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvCustomerName.setText(customerName)
        binding.tvCustomerCode.setText(customerCode)
        binding.tvSiteAddress.setText(siteAddress)
        if (visitWithName == null || visitWithName == "null") {
            binding.cvComp.visibility = View.GONE
        } else {
            binding.cvComp.visibility = View.VISIBLE
        }
        binding.tvQuestion.setText("هل أنت مع ${visitWithName}؟")

        binding.cbCompanionYes.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                if (SharedPreferencesHelper.getInstance().isAllowedToMakeRate()) {
                    binding.llComp.visibility = View.VISIBLE
                }

                binding.cbCompanionYes.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.colorPrimary)
                    )
                }

                if (binding.cbCompanionNo.isChecked == true) binding.cbCompanionNo.isChecked = false

                binding.cbCompanionNo.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.grey_color))
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.grey_color)
                    )
                }

            } else {
                binding.llComp.visibility = View.GONE
            }
        }

        binding.cbCompanionNo.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                binding.llComp.visibility = View.GONE

                binding.cbCompanionNo.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.colorPrimary)
                    )
                }

                if (binding.cbCompanionYes.isChecked == true) binding.cbCompanionYes.isChecked =
                    false

                binding.cbCompanionYes.apply {
                    setTextColor(ContextCompat.getColor(context, R.color.grey_color))
                    buttonTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.grey_color)
                    )
                }

            } else {
                if (SharedPreferencesHelper.getInstance().isAllowedToMakeRate()) {
                    binding.llComp.visibility = View.VISIBLE
                }
            }
        }

        binding.ratingBar.rating = 0f // Initial minimum rating

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                selectedRating = rating
                binding.tvRate.text = "${rating.toInt()} من 5"
            }
        }

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
//            if (convertDeveloperModeCheckToInt() == 1) {
//                DialogUtils.showResultDialog(
//                    context = requireContext(),
//                    message = "برجاء اغلاق وضع المطور ثم المحاولة مرة اخري",
//                    isSuccess = false,
//                    showOkButton = true
//                )
//                return@setOnClickListener
//            }

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
            if (binding.etVisibility.text.toString().isEmpty()) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "تقييم عرض الصنف مطلوب",
                    isSuccess = false,
                    showOkButton = true
                )
                return@setOnClickListener
            }
//            getCurrentLocation()
            val currentDistanceMeters = getCurrentDistanceMeters()

            Log.d("WHATdistance", currentDistanceMeters.toString())
            Log.d("WHATdistance", validGpsRange.toString())

            if (currentDistanceMeters == null) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "لم يتم تحديد موقعك الحالي",
                    description = "برجاء الانتظار حتى يتم تحديد موقع GPS ثم المحاولة مرة أخرى",
                    isSuccess = false,
                    showOkButton = true
                )
                return@setOnClickListener
            }

            if (currentDistanceMeters > (validGpsRange ?: 0)) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "خطأ في الموقع",
                    description =
                        "المسافة الحالية هي: %.1f متر\nيجب ألا تتجاوز %d متر للبدء"
                            .format(
                                currentDistanceMeters,
                                validGpsRange ?: 0
                            ),
                    isSuccess = false,
                    isLocation = true,
                    onReport = {
                        saveVisitGPS()
                    },
                )
                return@setOnClickListener
            }
            Log.d("WHATbtnSave", "Clicked")
//            checkInDate = getCurrentTimeTimestamp()
//            dateVisit = getCurrentDateTimestamp()
            saveVisitGPS()
        }

//        getCustomerData()
        fetchData()
    }

    private fun calculateDistance(
        currentLatitude: Double,
        currentLongitude: Double,
        customerLatitude: Double?,
        customerLongitude: Double?
    ): Double? {

        if (customerLatitude == null || customerLongitude == null) {
            Log.e("LocationService", "Customer location is null")
            return null
        }

        val currentLocation = Location("current").apply {
            latitude = currentLatitude
            longitude = currentLongitude
        }

        val customerLocation = Location("customer").apply {
            latitude = customerLatitude
            longitude = customerLongitude
        }

        val distanceMeters = currentLocation.distanceTo(customerLocation)

        val distanceKm = distanceMeters / 1000.0

        Log.d(
            "LocationService",
            "Distance = %.2f KM".format(distanceKm)
        )

        return distanceKm
    }

    private fun askPermission() {

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                // PERMISSION NOT GRANTED
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                    REQUEST_CODE
                )
                ProgressDialogHelper().errorMessage(
                    requireContext(),
                    "This app needs you to allow Location permission" +
                            "you Should allow it"
                )
            } else {
                try {
                    EventBus.getDefault().register(this)
                } catch (e: Exception) {
                    Log.d(TAG, "askPermissionError ${e.message.toString()}")
                }
            }

        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            REQUEST_CODE
        )
    }

//    @SuppressLint("SetTextI18n")
//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun observeLocation() {
//        viewModel.stopLocationUpdates()
//        viewModel.getCurrentLocation(customerLatitude, customerLongitude)
//        lifecycleScope.launch {
//            viewModel.locationState.collect { location ->
//                location?.let {
//
//                    if (customerLatitude != null && customerLongitude != null) {
//                        val distanceInMeters = myLocation.distanceTo(customerLocation)
//                        val formattedDistance = String.format("%.1f", distanceInMeters)
//                        binding.tvDistance.text = "$formattedDistance متر"
//                    } else {
//                        binding.tvDistance.text = "جارى تحديد الموقع"
//                        Log.w("observeLocation", "Failed to parse latitude or longitude")
//                    }
//
//                    Log.d(
//                        "Locationnnnnnnnnnnnnnnn",
//                        "Lat: ${it.latitude}, Lon: ${it.longitude}, Accuracy: ${it.accuracy} meters"
//                    )
//                }
//            }
//        }
//    }

    private fun saveVisitGPS() {

        if (visitWithName != null && visitWithName != "null") {
            if (!binding.cbCompanionYes.isChecked &&
                !binding.cbCompanionNo.isChecked
            ) {
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = "برجاء الإجابة عن سؤال هل أنت مع $visitWithName ؟",
                    isSuccess = false,
                    showOkButton = true
                )
                return
            }
        }

        val currentDistanceMeters = viewModel.distanceMeters.value
        val currentLocation = getCurrentLocationValue()

        if (currentLocation == null) {
            DialogUtils.showResultDialog(
                context = requireContext(),
                message = "الموقع الحالي غير متاح",
                isSuccess = false,
                showOkButton = true
            )
            return
        }

        if (currentDistanceMeters == null) {
            DialogUtils.showResultDialog(
                context = requireContext(),
                message = "الموقع الحالي غير متاح",
                description = "برجاء الانتظار حتى يتم تحديد الموقع الحالي",
                isSuccess = false,
                showOkButton = true
            )
            return
        }

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
                        act_target = if (actTarget.isNotEmpty()) {
                            actTarget.toInt()
                        } else {
                            0
                        },
                        another_order_type = anotherOrderType,
                        comment = comment,
                        check_in = checkIn,
                        phone_visit = phoneVisit,
                        device_type = "Android",
                        latitude = currentLocation.latitude.toString(),
                        longitude = currentLocation.longitude.toString(),

                        zone_flag =
                            if (currentDistanceMeters <= (validGpsRange ?: 0))
                                "IN"
                            else
                                "ERROR",

                        rate =
                            if (binding.cbCompanionYes.isChecked)
                                selectedRating.toString()
                            else
                                "",

                        rate_comment =
                            if (binding.cbCompanionYes.isChecked)
                                binding.etComment.text.toString()
                            else
                                "",

                        visit_with_confirmed =
                            if (binding.cbCompanionYes.isChecked)
                                "1"
                            else
                                "0",

                        visit_with_user_id =
                            if (visitWithUserId != null &&
                                visitWithUserId != "null"
                            ) {
                                visitWithUserId
                            } else {
                                null
                            },
                    )
                )
            )
        }
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
                            val data =
                                Gson().fromJson(
                                    it.data.data,
                                    Data::class.java
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
//                            if (!SharedPreferencesHelper.getInstance().isAllowedToMakeOrder()) {
//                                DialogUtils.showResultDialog(
//                                    context = requireContext(),
//                                    message = "غير مسموح لك بعمل طلبيات , الرجاء التواصل مع الإدارة المالية",
//                                    isSuccess = true,
//                                    showOkButton = true,
//                                    onOk = {
//                                        MainActivity.binding.navView2.visibility = View.VISIBLE
//                                        findNavController().navigate(
//                                            R.id.toHome
//                                        )
//                                    }
//                                )
//                                return@collect
//                            }
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
                            } else {
                                if (!SharedPreferencesHelper.getInstance().isAllowedToMakeOrder()) {
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
                                } else {
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
                                                    putString("fragment", "Gps")
                                                }

                                                findNavController().navigate(
                                                    R.id.toOrderCreationCycle, bundle
                                                )
                                            } else {
                                                MainActivity.binding.navView2.visibility =
                                                    View.VISIBLE
                                                findNavController().navigate(
                                                    R.id.toHome
                                                )
                                            }
                                        })
                                }
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
                                }
                            )
                        }
                    }

                    is Visits2Status.VisitsSelect -> {
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
                                            requireContext(), LoginActivity2::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                })
                        }
//                        binding.tryAgainButtons.root.visibility = View.GONE

                    }

                    is Visits2Status.Error -> {
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

    fun endTimer(): Long {
        dateVisit = System.currentTimeMillis() / 1000
        timerHandler.removeCallbacks(timerRunnable)
        return dateVisit
    }

    private fun isDeveloperModeEnabled(): Boolean {
        return Settings.Secure.getInt(
            requireContext().contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) == 1
    }

    private fun convertDeveloperModeCheckToInt(): Int {
        isDeveloperModeEnable = if (isDeveloperModeEnabled()) {
            1
        } else {
            0
        }
        return isDeveloperModeEnable
    }

    private fun parseApiDate(date: String): Long {
        val format = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.getDefault()
        )

        return format.parse(date)?.time ?: 0L
    }

//    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
//    private fun getCurrentLocation() {
//        viewModel.getCurrentLocation(
//            customerLatitude = customerLatitude,
//            customerLongitude = customerLongitude
//        )
//    }

    private fun observeDistance() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.distanceKm.collect { distanceKm ->

                    if (distanceKm == null) {
                        binding.tvDistance.text = "جارى تحديد الموقع"
                        return@collect
                    }

                    binding.tvDistance.text =
                        "%.2f KM".format(distanceKm)

                    Log.d(
                        "GpsVisitFragment",
                        "Current distance: %.2f meters"
                            .format(distanceKm * 1000.0)
                    )
                }
            }
        }
    }
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override fun onResume() {
        super.onResume()

        RequestPermission().enableLocation(requireActivity())
        requestPermission.permissionCheck(requireActivity())

        viewModel.getCurrentLocation(
            customerLatitude = customerLatitude,
            customerLongitude = customerLongitude
        )

        DefaultLocationClient(
            requireContext(),
            null
        ).checkGpsOpened(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        binding.tvDistance.text = ""
        // Stop Service And Stop EventBus From Fetch Location in onUpdateLocation Function
        Intent(requireContext(), GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_STOP
            requireActivity().startService(this)
        }
    }

    private fun getCurrentDistanceMeters(): Double? {
        return viewModel.distanceMeters.value
    }

    private fun getCurrentLocationValue(): Location? {
        return viewModel.locationState.value
    }

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(
                    imeInsets.bottom,
                    systemBars.bottom
                )
            )
            insets
        }
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