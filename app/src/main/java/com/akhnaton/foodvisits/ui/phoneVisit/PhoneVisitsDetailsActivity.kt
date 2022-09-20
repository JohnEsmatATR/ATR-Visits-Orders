package com.akhnaton.foodvisits.ui.phoneVisit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visits.SitesData
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityVisitsDetailsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.MainActivity
import com.akhnaton.foodvisits.ui.paymentType.PaymentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch


class PhoneVisitsDetailsActivity : AppCompatActivity(), LocationListener, View.OnClickListener {
    companion object {
        private const val TAG = "VisitsDetailsActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: PhoneVisitsViewModel by viewModels()
    private lateinit var binding: ActivityVisitsDetailsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionCode = 2
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

        setSpinnerAdapter()
        fetchData()
        getLocation()
        openMap()
        initWrongLocationDialog()
    }


    private fun getLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, this)
        val location =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude

            binding.fieldLongitude.text = latitude.toString()
            binding.fieldLatitude.text = longitude.toString()
            Log.d(TAG, "longitude: ${location.longitude} + latitude: ${location.latitude}")
        }

    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(
            this,
            "Long: ${location.longitude} + Lat: ${location.latitude}",
            Toast.LENGTH_SHORT
        ).show()

        Log.d(
            TAG,
            "Long: ${location.latitude} + Lat: ${location.longitude}",
        )
        if (!location.latitude.equals("")) {
            longitude = location.longitude
            latitude = location.latitude
            binding.fieldLongitude.text = latitude.toString()
            binding.fieldLatitude.text = longitude.toString()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                getLocation()

            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.SavePhoneVisits -> {
                        Log.d(TAG, "fetchData: ${it.data.data.visit_id}")

                        if (binding.visitType.selectedItemId.toInt() == 1) {
                            Log.d(TAG, "Visit Type == 1 ( سلبي ) ")
                            finishAffinity()
                            startActivity(
                                Intent(this@PhoneVisitsDetailsActivity, MainActivity::class.java)
                            )

                        } else {
                            Log.d(TAG, "Visit Type == 0 ( طلبية ) ")
                            startActivity(
                                Intent(this@PhoneVisitsDetailsActivity, PaymentActivity::class.java)
                                    .putExtra("customerPartySiteId", customerPartySiteId)
                                    .putExtra("orderType", orderType)
                                    .putExtra("customerTypePosition", customerTypePosition)
                                    .putExtra("visitId", it.data.data.visit_id.toString())
                            )
                        }
                    }
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
//        myLocation.latitude = 30.5082604
//        myLocation.longitude = 31.4061817
        val distanceInMeters = customerLocation.distanceTo(myLocation)

        if (distanceInMeters < 100.0) {
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
                    dateVisit = ConvertDate.getDateTimeStamp() // Visit Send With end Date
                )
            )
        }
    }

}