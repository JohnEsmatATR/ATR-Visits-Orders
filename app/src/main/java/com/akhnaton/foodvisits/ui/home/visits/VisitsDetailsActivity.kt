package com.akhnaton.foodvisits.ui.home.visits

import android.app.Activity
import android.content.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityVisitsDetailsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.home.visits.paymentType.PaymentActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promoterDayDetails.PromoterDayDetailsActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems.PromoterItemsActivity
import com.akhnaton.foodvisits.ui.home.visits.promoters.promotersUploadImages.PromotersActivity
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

class VisitsDetailsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "VisitsDetailsActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: VisitsViewModel by viewModels()
    private lateinit var binding: ActivityVisitsDetailsBinding
    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var limitArea: Int = 0
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var enteredTime = ""
    private var zoneFlag = ""
    private lateinit var customerData: CustomerVisitPlan
    private lateinit var progressBar: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visits_details)

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        enteredTime = intent.getStringExtra("time").toString()
        customerData = intent.getSerializableExtra("customerSiteData") as CustomerVisitPlan

        binding.custName.text = customerData.customer_name
        binding.custAddress.text = customerData.customer_address
        binding.custCode.text = customerPartySiteId


        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.saveVis.setOnClickListener(this)

        setSpinnerAdapter()
        fetchData()
        openMap()
        initWrongLocationDialog()
        promotersUploadPhotos()
        promotersAddStockStatus()

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
                    is VisitsStatus.Idle -> Log.d(TAG, "fetchData: ")
                    is VisitsStatus.Loading -> Log.d(TAG, "fetchData: ")

                    is VisitsStatus.SaveVisits -> {
                        Log.d(TAG, "fetchData: ${it.data.data.visit_id}")
                        startActivity(
                            Intent(this@VisitsDetailsActivity, PaymentActivity::class.java)
                                .putExtra("customerPartySiteId", customerPartySiteId)
                                .putExtra("orderType", orderType)
                                .putExtra("customerTypePosition", customerTypePosition)
                                .putExtra("visitId", it.data.data.visit_id.toString())
                        )
                    }
                    is VisitsStatus.GetAppSetting -> {
                        limitArea = it.data.data.limit_area
                        Log.d(TAG, "LimitArea======: $limitArea")
                    }
                    is VisitsStatus.Error -> Log.d(TAG, "Error====== ${it.error}")

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
        compareLocation()
    }

    private fun customerLocationMissing(): String {
        return if (longitude.toString() == "" || latitude.toString() == "") {
            "IN"
        } else {
            "ERROR"
        }
    }

    private fun compareLocation() {
        if (customerData.customer_latitude == "") {
            zoneFlag = "IN"
            saveVisits()
        } else {
            val customerLocation = Location("")
            customerLocation.latitude = latitude
            customerLocation.longitude = longitude

            val myLocation = Location("")
            myLocation.latitude = customerData.customer_latitude.toDouble()
            myLocation.longitude = customerData.customer_longitude.toDouble()

            val distanceInMeters = customerLocation.distanceTo(myLocation)

            if (distanceInMeters < limitArea) {
                zoneFlag = "IN"
                saveVisits()

            } else {
                zoneFlag = customerLocationMissing()
                progressBar.show()
            }
        }
    }

    private fun openMap() {
        if (customerData.customer_latitude.equals("")){
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
            }
        progressBar.setCancelable(false);

    }

    private fun saveVisits() {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.SaveVisit(
                    version = versionName,
                    token = SharedPreferencesHelper.getInstance().getUserToken(),
                    customerPartySiteId = customerPartySiteId,
                    visitType = SpinnerHelper().getVisitTypeFromSpinner(binding.visitType), // A -> طلبية --- C -> سلبي
                    visitTarget = binding.visTarget.text.toString().trim(),
                    visitActualTarget = binding.actTarget.text.toString().trim(),
                    latitude = latitude.toString(),
                    longtitude = longitude.toString(),
                    deviceType = "Mob",
                    zoneFlag = zoneFlag, // IN == Correct Location -- ERROR == Wrong Location
                    checkInDate = enteredTime.toString(), // Date Entered
                    dateVisit = ConvertDate.getDateTimeStamp(), // Visit Send With end Date
                    phoneVisit = false
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        RequestPermission().enableLocation(this)
        requestPermission.permissionCheck(this)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("GPSLocationUpdates"))

        lifecycleScope.launch {
            viewModel.visitsIntent.send(VisitsIntent.GetAppSetting(versionName))
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val latitudeExtra = intent.getStringExtra("latitude")
            val longitudeExtra = intent.getStringExtra("longitude")

            latitude = latitudeExtra?.toDouble() ?: 0.00
            longitude = longitudeExtra?.toDouble() ?: 0.00

            binding.fieldLongitude.text = latitude.toString()
            binding.fieldLatitude.text = longitude.toString()
        }
    }

    private fun promotersUploadPhotos() {
        binding.btnPromotersImages.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromotersActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getUserToken())
            startActivity(intent)
            Log.d("jkfvjkfjkf", "promotersUploadPhotos: " + customerPartySiteId + " | " + customerData.customer_party_site_id)
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
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getUserToken())
            startActivity(intent)
        })
        binding.btnPromotersDetails.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(
                this@VisitsDetailsActivity,
                PromoterDayDetailsActivity::class.java
            )
            intent.putExtra("cust_code", customerData.customer_name)
            intent.putExtra("party_site", customerData.customer_party_site_id)
            intent.putExtra("employee_id", SharedPreferencesHelper.getInstance().getUserToken())
            startActivity(intent)
        })
//        binding.btnPromotersCompetitors.setOnClickListener(View.OnClickListener { v: View? ->
//            val intent = Intent(
//                this@VisitsDetailsActivity,
//                PromoterCompetitorsActivity::class.java
//            )
//            intent.putExtra("cust_code", code)
//            intent.putExtra("party_site", party_site)
//            intent.putExtra("employee_id", employee_id)
//            startActivity(intent)
//        })
    }
}