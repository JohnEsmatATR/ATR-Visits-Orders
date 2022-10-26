package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.databinding.ActivityVisitsDetailsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.visits.paymentType.PaymentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch


class PhoneVisitsDetailsActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "VisitsDetailsActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: ActivityVisitsDetailsBinding
    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private var limitArea: Int = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var enteredTime = ""
    private var zoneFlag = ""
    private lateinit var customerData: SitesData
    private lateinit var progressBar: SweetAlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_visits_details)

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        enteredTime = intent.getStringExtra("time").toString()
        customerData = intent.getSerializableExtra("customerSiteData") as SitesData

        binding.custName.text = customerData.customer_name
        binding.custAddress.text = customerData.customer_addresses
        binding.custCode.text = customerPartySiteId

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.saveVis.setOnClickListener(this)

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

        setSpinnerAdapter()
        fetchData()
        openMap()
        initWrongLocationDialog()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.SavePhoneVisits -> {
                        if (binding.visitType.selectedItemId.toInt() == 1) {
                            startActivity(
                                Intent(
                                    this@PhoneVisitsDetailsActivity,
                                    MainActivity::class.java
                                )
                            )
                            finishAffinity()

                        } else {
                            startActivity(
                                Intent(this@PhoneVisitsDetailsActivity, PaymentActivity::class.java)
                                    .putExtra("customerPartySiteId", customerPartySiteId)
                                    .putExtra("orderType", orderType)
                                    .putExtra("customerTypePosition", customerTypePosition)
                                    .putExtra("visitId", it.data.data.visit_id.toString())
                            )
                        }
                    }
                    is PhoneVisitsStatus.GetAppSetting -> limitArea = it.data.data.limit_area
                    is PhoneVisitsStatus.Error -> Log.d(TAG, "Error=== ${it.error}")
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
        val customerLocation = Location("")
        customerLocation.latitude = latitude
        customerLocation.longitude = longitude

        val myLocation = Location("")
        myLocation.latitude = latitude
        myLocation.longitude = longitude
        val distanceInMeters = customerLocation.distanceTo(myLocation)

        if (distanceInMeters < limitArea) {
            zoneFlag = "IN"
            saveVisits()

        } else {
            zoneFlag = customerLocationMissing()
            progressBar.show()
        }
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
                zoneFlag = "ERROR"
                sDialog.dismissWithAnimation()
                saveVisits()
            }
        progressBar.setCancelable(false);

    }

    override fun onResume() {
        super.onResume()
        requestPermission.enableLocation(this)
        requestPermission.permissionCheck(this)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("GPSLocationUpdates"))

        lifecycleScope.launch {
            viewModel.phoneVisitsIntent.send(PhoneVisitsIntent.GetAppSetting(versionName))
        }
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
                    latitude = latitude.toString(),
                    longtitude = longitude.toString(),
                    deviceType = "Mob",
                    zoneFlag = zoneFlag, // IN == Correct Location -- ERROR == Wrong Location
                    checkInDate = enteredTime.toString(), // Date Entered
                    dateVisit = ConvertDate.getDateTimeStamp(), // Visit Send With end Date
                    phoneVisit = true
                )
            )
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

}