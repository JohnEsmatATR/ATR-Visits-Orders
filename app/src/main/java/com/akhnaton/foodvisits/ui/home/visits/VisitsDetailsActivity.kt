package com.akhnaton.foodvisits.ui.home.visits

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.interfaces.location.ILocationClient
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityVisitsDetailsBinding
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.domin.VisitsRepository
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.shared.location.DefaultLocationClient
import com.akhnaton.foodvisits.shared.location.GetLocationService
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.visits.paymentType.PaymentActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promoterCompetitorsActivity.PromoterCompetitorsActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promoterDayDetails.PromoterDayDetailsActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems.PromoterItemsActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promotersUploadImages.PromotersActivity
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.REQUEST_CODE
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class VisitsDetailsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "VisitsDetailsActivity"
    }

    private lateinit var checkConnection: CheckConnection
    private lateinit var viewModel: VisitsViewModel
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var binding: ActivityVisitsDetailsBinding
    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private var limitArea: Int = 0
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var enteredTime = ""
    private var customerName = ""
    private var zoneFlag = ""
    private lateinit var customerData: CustomerVisitPlan
    private lateinit var progressBar: SweetAlertDialog
    val customerLocation = Location("")
    private lateinit var locationClient: ILocationClient
    val myLocation = Location("")

    private lateinit var dialog: ProgressDialog
    private var isVisitHandled = false
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visits_details)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewModel = ViewModelProvider(
            this,
            VisitsViewModelFactory(baseContext)
        )[VisitsViewModel::class.java]
        checkConnection = CheckConnection(baseContext)
        binding.versionName.text=versionName

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
//        enteredTime = intent.getStringExtra("time").toString()
        enteredTime = intent.getStringExtra("time").toString()
        customerData = intent.getSerializableExtra("customerSiteData") as CustomerVisitPlan
        customerName = intent.getStringExtra("customer_name").toString()

        binding.custName.text = customerData.customer_name
        binding.custAddress.text = customerData.customer_address
        binding.custCode.text = customerPartySiteId

        binding.fieldLongitude.text = ""
        binding.fieldLatitude.text = ""
        binding.accurate.text = ""
        locationClient = DefaultLocationClient(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.saveVis.setOnClickListener(this)
        dialog = ProgressDialogHelper().showAlertProgress(
           this@VisitsDetailsActivity,
            "Loading..."
        )

        askPermission()
        setSpinnerAdapter()
        fetchData()
        openMap()
        initWrongLocationDialog()
        promotersUploadPhotos()
        promotersAddStockStatus()
        checkPromoters()
        observeTimer()
        observeLocation()
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                when (result.resultCode) {
                    locationPermissionCode -> when (result.resultCode) {
                        RESULT_OK -> Log.d("abc", "OK")
                        RESULT_CANCELED -> RequestPermission().enableLocation(this)
                    }
                }
            }
        }
    }

    private fun observeTimer() {
        viewModel.stopTimer()
        viewModel.resetTimer()
        viewModel.startTimer()
        lifecycleScope.launch {
            viewModel.timerState.collect { timeStaring ->
                binding.timmer.text = timeStaring
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun observeLocation() {
        viewModel.stopLocationUpdates()
        viewModel.getCurrentLocation()
        lifecycleScope.launch {
            viewModel.locationState.collect { location ->
                location?.let {

                    binding.fieldLongitude.text = it.longitude.toString()
                    binding.fieldLatitude.text = it.latitude.toString()
                    binding.accurate.text = "${String.format("%.1f", it.accuracy)} متر"


                    myLocation.latitude = it.latitude
                    myLocation.longitude = it.longitude


                    val customerLocation = Location("")
                    val customerLat = customerData.customer_latitude
                    val customerLng = customerData.customer_longitude

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
        val check = SharedPreferencesHelper.getInstance().getProm()
        Log.d("vhjbhjvbfvfvv", "checkPromoters: $check")
        if (!check) {
            binding.btnPromotersImages.visibility = View.GONE
            binding.btnPromotersStockStatus.visibility = View.GONE
            binding.btnPromotersDetails.visibility = View.GONE
            binding.btnPromotersCompetitors.visibility = View.GONE
        } else {
            binding.btnPromotersImages.visibility = View.VISIBLE
            binding.btnPromotersStockStatus.visibility = View.VISIBLE
            binding.btnPromotersDetails.visibility = View.VISIBLE
            binding.btnPromotersCompetitors.visibility = View.VISIBLE
        }
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.statusVisit.collect {
                when (it) {
                    is VisitsStatus.Idle -> {
                        dialog.show()
                        Log.d(TAG, "fetchDataSaveVisits: Idle")
                    }
                    is VisitsStatus.Loading ->{
                        dialog.show()
                        Log.d(TAG, "fetchDataSaveVisits: Loading")
                        binding.constrain.isEnabled=false
                    }

                    is VisitsStatus.SaveVisits -> {
                        dialog.hide()
                        if (!isVisitHandled) {
                            isVisitHandled = true
                            checkConnection.deleteSaveVisitFromDB()
                            val visits = checkConnection.getVisits()
                            Log.d(TAG, "fetchDataSaveVisits: $visits")

                            binding.constrain.isEnabled=false
                        } else {
                            Log.d(TAG, "fetchDataSaveVisits: تم المعالجة مسبقًا")
                            binding.constrain.isEnabled=true
                        }
                    }

                    is VisitsStatus.GetAppSetting -> {
                        dialog.hide()
                        try {
                            limitArea = it.data!!.data.limit_area
                            Log.d(TAG, "LimitArea======: $limitArea")
                        } catch (e: Exception) {
                            limitArea = 100
                            Log.d(TAG, "Exception======: $limitArea")
                        }
                    }

                    is VisitsStatus.Error -> {
                        dialog.hide()
                      //      checkVisitSituation("")
                        Log.d(TAG, "fetchDataSaveVisits1111Error${it.error}")
                        binding.constrain.isEnabled=true
                    }

                    else -> {}
                }
            }
        }
    }

//    private fun checkVisitSituation(visitId: String) {
//        val check = SharedPreferencesHelper.getInstance().getMakeOrder()
//        if (checkConnection.checkConnection()) {
//            if (binding.visitType.selectedItem.toString() != null || !check) {
//                startActivity(Intent(this@VisitsDetailsActivity,MainActivity::class.java))
//            }
//        }
//        else {
//            startActivity(Intent(this@VisitsDetailsActivity,MainActivity::class.java))
//        }
//    }

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
        if (customerLocation == null) {
            ProgressDialogHelper().errorMessage(this@VisitsDetailsActivity, "خطا فى الموقع")
        } else if (binding.visTarget.text.isEmpty()) {
            binding.visTarget.error = "ادخل هدف الزيارة"
            binding.visTarget.requestFocus()

        } else {

            compareLocation()
        }


    }

    private fun customerLocationMissing(): String {
        return if (customerLocation?.longitude.toString() == "" || customerLocation?.latitude.toString() == "") {
            "IN"
        } else {
            "ERROR"
        }
    }


    private fun compareLocation() {
        if (isDeveloperModeEnabled()) {
            if (customerData.customer_latitude == "") {
                zoneFlag = "IN"
                saveVisits()
            } else {
                val customerLocation = Location("")
                customerLocation.latitude = customerData.customer_latitude.toDouble()
                customerLocation.longitude = customerData.customer_longitude.toDouble()

                val distanceInMeters = myLocation.distanceTo(customerLocation)

                if (distanceInMeters < limitArea) {
                    zoneFlag = "IN"
                    saveVisits()
                    Log.d(TAG, "compareLocation: $limitArea")
                } else {
                    zoneFlag = customerLocationMissing()
                    progressBar.show()

                }
            }
        } else {
            ProgressDialogHelper().gpsAlert(this)
        }
    }


    private fun openMap() {
        if (customerData.customer_latitude.equals("")) {
            binding.btnMap.visibility = View.GONE
        }
        binding.btnMap.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=${customerData.customer_latitude},${customerData.customer_longitude}")
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
                zoneFlag = "ERROR"
                sDialog.dismissWithAnimation()
                saveVisits()
                progressBar.dismiss()
            }
        progressBar.setCancelable(false)

    }

    private fun saveVisits() {
        dialog.show()
        val long = binding.fieldLongitude.text.toString()
        val lat = binding.fieldLatitude.text.toString()

        if (long.isBlank() || lat.isBlank()) {
            val snackbar = Snackbar.make(binding.root, "لم يتم الوصول لبيانات الخريطة", Snackbar.LENGTH_LONG)
            snackbar.setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            snackbar.show()
            dialog.hide()
            return
        }
        lifecycleScope.launch {
            val repository = VisitsRepository(this@VisitsDetailsActivity)

            val result = repository.saveVisit(
                version = versionName,
                token = SharedPreferencesHelper.getInstance().getUserToken(),
                customerPartySiteId = customerPartySiteId,
                visitType = SpinnerHelper().getVisitTypeFromSpinner(binding.visitType),
                visitTarget = binding.visTarget.text.toString().trim(),
                visitActualTarget = binding.actTarget.text.toString().trim(),
                latitude = lat,
                longitude = long,
                deviceType = "Mob",
                zoneFlag = zoneFlag,
                checkInDate = enteredTime.toString(),
                dateVisit = (System.currentTimeMillis() / 1000).toString(),
                customerType = customerTypePosition,
                orderType = orderType
            )

            val message = if (result.status == 200) {
                "تم حفظ الزيارة أونلاين بنجاح"

            } else {
                "تم حفظ الزيارة أوفلاين، وسيتم إرسالها لاحقًا"
            }

            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(
                    if (result.status == 200)
                        ContextCompat.getColor(this@VisitsDetailsActivity, R.color.green)
                    else
                        ContextCompat.getColor(this@VisitsDetailsActivity, R.color.gray)
                )
                .show()


            delay(1500)
            startActivity(Intent(this@VisitsDetailsActivity, MainActivity::class.java))
        }


    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()
        RequestPermission().enableLocation(this)
        requestPermission.permissionCheck(this)
        observeLocation()
        lifecycleScope.launch {
            viewModel.visitsIntent.send(VisitsIntent.GetAppSetting(versionName))
        }

        //Start Service And Check if GPS Opened or not
        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_START
            startService(this)
        }

        DefaultLocationClient(
            this@VisitsDetailsActivity,
            null
        ).checkGpsOpened(this@VisitsDetailsActivity)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        binding.fieldLongitude.text = ""
        binding.fieldLatitude.text = ""
        binding.accurate.text = ""
        // Stop Service And Stop EventBus From Fetch Location in onUpdateLocation Function
        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_STOP
            startService(this)
        }
        progressBar.dismiss()
    }

//    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            val latitudeExtra = intent.getStringExtra("latitude")
//            val longitudeExtra = intent.getStringExtra("longitude")
//
//            val latitude = latitudeExtra?.toDouble() ?: 0.00
//            val longitude = longitudeExtra?.toDouble() ?: 0.00
//
//            binding.fieldLongitude.text = latitude.toString()
//            binding.fieldLatitude.text = longitude.toString()
//        }
//    }

    private fun promotersUploadPhotos() {
        binding.btnPromotersImages.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromotersActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("token", SharedPreferencesHelper.getInstance().getUserToken())
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getEmployeeId())
            intent.putExtra("customer_code", customerData.CUSTOMER_CODE)
            startActivity(intent)
            Log.d(
                "jkfvjkfjkf",
                "promotersUploadPhotos: " + customerPartySiteId + " | " + customerData.customer_party_site_id
            )
        })
    }

    private fun promotersAddStockStatus() {
        binding.btnPromotersStockStatus.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromoterItemsActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("token", SharedPreferencesHelper.getInstance().getUserToken())
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getEmployeeId())
            intent.putExtra("customer_code", customerData.CUSTOMER_CODE)
            startActivity(intent)
        })
        binding.btnPromotersDetails.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromoterDayDetailsActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("token", SharedPreferencesHelper.getInstance().getUserToken())
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getEmployeeId())
            intent.putExtra("customer_code", customerData.CUSTOMER_CODE)
            startActivity(intent)
        })
        binding.btnPromotersCompetitors.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromoterCompetitorsActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("token", SharedPreferencesHelper.getInstance().getUserToken())
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getEmployeeId())
            intent.putExtra("customer_code", customerData.CUSTOMER_CODE)
            startActivity(intent)
        })
    }

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
                    REQUEST_CODE
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
            REQUEST_CODE
        )
    }


    private fun isDeveloperModeEnabled(): Boolean {
        return Settings.Secure.getInt(
            this.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRestart() {
        super.onRestart()
        binding.fieldLongitude.text = ""
        binding.fieldLatitude.text = ""
        binding.accurate.text = ""
        observeLocation()
    }
}