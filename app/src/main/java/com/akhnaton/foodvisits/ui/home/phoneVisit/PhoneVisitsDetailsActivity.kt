package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.interfaces.location.ILocationClient
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityVisitsDetailsBinding
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.shared.location.DefaultLocationClient
import com.akhnaton.foodvisits.shared.location.GetLocationService
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModelFactory
import com.akhnaton.foodvisits.ui.home.visits.paymentType.PaymentActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class PhoneVisitsDetailsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "VisitsDetailsActivity"
    }

    private lateinit var checkConnection: CheckConnection
    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var visitViewModel: VisitsViewModel
    private lateinit var binding: ActivityVisitsDetailsBinding
    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private var limitArea: Int = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var customerPartySiteId = ""
    private var customerCode = ""
    private var customerName = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var enteredTime = ""
    private var zoneFlag = ""
    private lateinit var customerData: SitesData
    private lateinit var progressBar: SweetAlertDialog
    val customerLocation = Location("")
    private lateinit var locationClient: ILocationClient
    val myLocation = Location("")


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visits_details)


        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        enteredTime = intent.getStringExtra("time").toString()
        customerCode = intent.getStringExtra("customer_code").toString()
        customerName = intent.getStringExtra("customer_name").toString()
        customerData = intent.getSerializableExtra("customerSiteData") as SitesData
        Log.d("DEBUG_DATA", "Received Name: $customerName")
        Log.d("DEBUG_DATA", "Received Address: $customerCode")
        visitViewModel = ViewModelProvider(
            this,
            VisitsViewModelFactory(baseContext)
        )[VisitsViewModel::class.java]

        binding.custName.text = customerName
        binding.custAddress.text = customerData.customer_name
        binding.custCode.text = customerData.customer_party_site_id


        Log.d("jnjndcbvnj", "onCreate: ${customerData.customer_name} | ${customerData.customer_addresses}")
        Log.d("jnjndcbvnj", "onCreate: ${customerData.customer_addresses}")


        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.saveVis.setOnClickListener(this)

        locationClient = DefaultLocationClient(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )

        checkConnection = CheckConnection(baseContext)


        binding.fieldLongitude.text = ""
        binding.fieldLatitude.text =  ""
        binding.accurate.text = ""
        locationClient = DefaultLocationClient(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )

        askPermission()
        checkPromoters()
        checkLocationPromotion()
        observeTimer()
        observeLocation()
        setSpinnerAdapter()
        fetchData()
        openMap()
        initWrongLocationDialog()
    }
    private fun observeTimer(){
        visitViewModel.stopTimer()
        visitViewModel.resetTimer()
        lifecycleScope.launch {
            visitViewModel.timerState.collect{timeStaring ->
                binding.timmer.text=timeStaring
            }
        }
    }
    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun observeLocation() {
        visitViewModel.stopLocationUpdates()
        visitViewModel.getCurrentLocation()
        lifecycleScope.launch {
            visitViewModel.locationState.collect { location ->
                location?.let {

                    binding.fieldLongitude.text = it.longitude.toString()
                    binding.fieldLatitude.text = it.latitude.toString()
                    binding.accurate.text = "${it.accuracy} متر "


                    myLocation.latitude = it.latitude
                    myLocation.longitude = it.longitude


                    val customerLocation = Location("")
                    val customerLat = customerData.customer_latitude
                    val customerLng = customerData.customer_longitude
                    Log.d(TAG, "observeLocation: $customerLat")

                    val isLatValid = !customerLat.isNullOrBlank()
                    val isLngValid = !customerLng.isNullOrBlank()

                    if (isLatValid && isLngValid) {
                        val lat = customerLat!!.toDoubleOrNull()
                        val lng = customerLng!!.toDoubleOrNull()

                        if (lat != null && lng != null) {
                            customerLocation.latitude = lat
                            customerLocation.longitude = lng

                            val distanceInMeters = myLocation.distanceTo(customerLocation)
                            val formattedDistance = String.format("%.1f", distanceInMeters)
                            binding.distanceBetweenCustomer.text = "$formattedDistance متر"
                        } else {
                            binding.distanceBetweenCustomer.text = "الموقع غير متاح"
                            Log.w("observeLocation", "Failed to parse latitude or longitude")
                        }
                    } else {
                        binding.distanceBetweenCustomer.text = "الموقع غير متاح"
                        Log.w("observeLocation", "Latitude or longitude is blank")
                    }

                    Log.d(
                        "Locationnnnnnnnnnnnnnnn",
                        "Lat: ${it.latitude}, Lon: ${it.longitude}, Accuracy: ${it.accuracy} meters"
                    )
                }
            }
        }
    }
    private fun checkPromoters() {
        binding.btnPromotersImages.visibility = View.GONE
        binding.btnPromotersStockStatus.visibility = View.GONE
        binding.btnPromotersDetails.visibility = View.GONE
        binding.btnPromotersCompetitors.visibility = View.GONE
    }

    fun checkLocationPromotion() {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (result.resultCode) {
                    locationPermissionCode -> when (result.resultCode) {
                        Activity.RESULT_OK -> Log.d("abc", "OK")
                        Activity.RESULT_CANCELED -> RequestPermission().enableLocation(this)
                    }
                }
            }
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.SavePhoneVisits -> {
                        checkVisitSituation(it.data.data.visit_id.toString())
                    }

                    is PhoneVisitsStatus.GetAppSetting -> limitArea = it.data.data.limit_area
                    is PhoneVisitsStatus.Error -> Log.d(TAG, "Error=== ${it.error}")
                    else -> {}
                }
            }
        }
    }

    private fun checkVisitSituation(visitId: String) {
        if (binding.visitType.selectedItemId.toInt() == 1) {
            startActivity(
                Intent(
                    this@PhoneVisitsDetailsActivity,
                    MainActivity::class.java
                )
            )
            finishAffinity()

        }
        else {
            val check = SharedPreferencesHelper.getInstance().getMakeOrder()
            if (checkConnection.checkConnection()) {
                if (binding.visitType.selectedItem.toString() == "سلبى" || !check) {
                    finish()
                } else {
                    startActivity(
                        Intent(this@PhoneVisitsDetailsActivity, PaymentActivity::class.java)
                            .putExtra("customerPartySiteId", customerPartySiteId)
                            .putExtra("orderType", orderType)
                            .putExtra("customerTypePosition", customerTypePosition)
                            .putExtra("customer_code", customerCode)
                            .putExtra("customer_name", customerName)
                            .putExtra("visitId", visitId)
                    )
                }
            }
            else {
                if (binding.visitType.selectedItem.toString() == "سلبى" || !check)
                {
                    finish()
                }
                else {
                    Toast.makeText(
                        baseContext,
                        "لا يمكن اكمال الطلبية لعدم توفر انترنت",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }


    private fun setSpinnerAdapter() {
        val mVisitTypeList: ArrayList<String> = ArrayList()
        mVisitTypeList.add("طلبية")
        mVisitTypeList.add("سلبى")

        SpinnerHelper().setNormalSpinnerAdapter(
            binding.visitType,
            mVisitTypeList.toMutableList(),
            this
        )
    }

    override fun onClick(onClick: View?) {
        if (binding.visTarget.text.isNotEmpty()) {
            compareLocation()
        } else {
            binding.visTarget.error = "ادخل هدف الزيارة"
            binding.visTarget.requestFocus()
        }
    }

//    private fun customerLocationMissing(): String {
////        return if (mCurrentLocation?.longitude.toString() == "" || mCurrentLocation?.latitude.toString() == "") {
////            "IN"
////        } else {
////            "ERROR"
////        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun updateLocation(locationData: LocationData) {
//        Log.i(TAG, "onUpdateLocationMain: " + locationData.latitude + " " + locationData.longitude)
//        val location = Location(LocationManager.GPS_PROVIDER)
//        location.latitude = locationData.latitude
//        location.longitude = locationData.longitude
//
//        binding.fieldLongitude.text = location.latitude.toString()
//        binding.fieldLatitude.text = location.longitude.toString()
//        mCurrentLocation = location
////        progressBar.dismiss()
//    }


    private fun compareLocation() {
        val customerLocation = Location("")
        if (customerData.customer_latitude != "" && customerData.customer_longitude != "") {
            customerLocation.latitude = customerData.customer_latitude.toDouble()
            customerLocation.longitude = customerData.customer_longitude.toDouble()
        }


        myLocation.latitude = binding.fieldLatitude.text.toString().toDouble()
        myLocation.longitude = binding.fieldLongitude.text.toString().toDouble()

        val distanceInMeters = myLocation.distanceTo(customerLocation)

        if (distanceInMeters < limitArea) {
            zoneFlag = "IN"
        } else {
            zoneFlag = "ERROR"
        }
        saveVisits()

    }


    private fun openMap() {
        binding.btnMap.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$longitude,$latitude")
            )
            startActivity(intent)
        }
    }

    private fun initWrongLocationDialog() {

        progressBar = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        progressBar.setTitleText("تنبيه!...")
            .setContentText("انت لست فى موقع العميل")
            .setConfirmText("OK")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                progressBar.dismiss()
            }
            .setCancelButton(
                "الابلاغ عن موقع خطأ"
            ) { sDialog ->

            }
        progressBar.setCancelable(false);

    }

    override fun onResume() {
        super.onResume()
        requestPermission.enableLocation(this)
        requestPermission.permissionCheck(this)

        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(PhoneVisitsIntent.GetAppSetting(versionName))
        }

        //Start Service And Check if GPS Opened or not
        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_START
            startService(this)
        }

        DefaultLocationClient(
            this@PhoneVisitsDetailsActivity,
            null
        ).checkGpsOpened(this@PhoneVisitsDetailsActivity)
    }

    override fun onPause() {
        super.onPause()
        // Stop Service And Stop EventBus From Fetch Location in onUpdateLocation Function
        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_STOP
            startService(this)
        }
        progressBar.dismiss()
    }

    private fun saveVisits() {

        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.SaveVisit(
                    version = versionName,
                    token = SharedPreferencesHelper.getInstance().getUserToken(),
                    customerPartySiteId = customerPartySiteId,
                    visitType = SpinnerHelper().getVisitTypeFromSpinner(binding.visitType), // A -> طلبية --- C -> سلبي
                    visitTarget = binding.visTarget.text.toString().trim(),
                    visitActualTarget = binding.actTarget.text.toString().trim(),
                    latitude = binding.fieldLatitude.text.toString(),
                    longtitude = binding.fieldLongitude.text.toString(),
                    deviceType = "Mob",
                    zoneFlag = zoneFlag, // IN == Correct Location -- ERROR == Wrong Location
                    checkInDate = enteredTime.toString(), // Date Entered
                    dateVisit = ConvertDate.getDateTimeStamp(), // Visit Send With end Date
                    customerType = customerTypePosition,
                    orderType = orderType,
                    phoneVisit = true,
                )
            )
        }
    }

//    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            val latitudeExtra = intent.getStringExtra("latitude")
//            val longitudeExtra = intent.getStringExtra("longitude")
//
//            latitude = latitudeExtra?.toDouble() ?: 0.00
//            longitude = longitudeExtra?.toDouble() ?: 0.00
//
//            binding.fieldLongitude.text = latitude.toString()
//            binding.fieldLatitude.text = longitude.toString()
//        }
//    }


    private fun askPermission() {

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                // PERMISSION NOT GRANTED
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                    ImagePicker.REQUEST_CODE
                )
                ProgressDialogHelper().errorMessage(
                    this,
                    "This app needs you to allow Location permission To Use Attendance " +
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
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            ImagePicker.REQUEST_CODE
        )
    }


}