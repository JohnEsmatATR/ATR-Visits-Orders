package com.akhnaton.foodvisits.ui.home.addCustomer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.model.visits.MainCustomerLine
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerIntent
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerStatus
import com.akhnaton.foodvisits.databinding.ActivityAddEmployeeBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import kotlinx.coroutines.launch
import java.util.Locale

class AddCustomerActivity : AppCompatActivity(), LocationListener, View.OnClickListener {

    companion object {
        private const val TAG = "AddCustomerActivity"
    }

    private lateinit var binding: ActivityAddEmployeeBinding
    private val viewModel: AddCustomerViewModel by viewModels()
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val customerType: MutableList<String> = ArrayList()
    private val lineType: MutableList<String> = ArrayList()
    private var linesList: List<LinesUsers> = ArrayList()
    private var mainList: List<MainCustomerLine> = ArrayList()
    private val mainLine: MutableList<String> = ArrayList()
    private var customerTypeList: List<CustomerType> = ArrayList()
    private var customerTypePosition: String = ""
    private var lineIdPosition: String = ""
    private var mainCustomerLinePosition: String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_employee)

        binding.customerType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            customerTypePosition = customerTypeList[position].customer_type_id
            lifecycleScope.launch {
                viewModel.customerIntent.send(
                    AddCustomerIntent.GetLines(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerTypePosition, "Food"
                    )
                )
            }
        }


        binding.lines.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            lineIdPosition = linesList[position].line_id

            lifecycleScope.launch {
                viewModel.customerIntent.send(
                    AddCustomerIntent.GetMainLines(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerTypePosition,
                        "Food",
                        lineIdPosition
                    )
                )
            }
        }

        binding.mainLine.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            mainCustomerLinePosition = mainList[position].customer_code
        }

        binding.addCustomerBtn.setOnClickListener(this)
        binding.backBtn.setOnClickListener{onBackPressedDispatcher.onBackPressed()}
        getLocation()
        getDataFromViewModel()
        fetchData()
    }

    private fun getDataFromViewModel() {
        lifecycleScope.launch {
            viewModel.customerIntent.send(
                AddCustomerIntent.GetCustomerType(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    AddCustomerStatus.Idle -> Log.d(TAG, "fetchData: Idle ")
                    AddCustomerStatus.Loading -> Log.d(TAG, "fetchData: Loading")

                    is AddCustomerStatus.GetCustomerType -> {
                        customerType.clear()
                        binding.customerType.text.clear()
                        it.data.data.user_customer_type.forEach { type -> customerType.add(type.customer_name) }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.customerType,
                            customerType, this@AddCustomerActivity
                        )
                        customerTypeList = it.data.data.user_customer_type
                    }

                    is AddCustomerStatus.GetLines -> {
                        lineType.clear()
                        binding.lines.text.clear()
                        it.data.data.user_lines.forEach { line -> lineType.add(line.line_name) }
                        linesList = it.data.data.user_lines
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.lines,
                            lineType, this@AddCustomerActivity
                        )

                    }

                    //Get Main Customer Lines
                    is AddCustomerStatus.GetMainLine -> {
                        mainLine.clear()
                        binding.mainLine.text.clear()
                        mainList = it.data.data.main_customer_line
                        it.data.data.main_customer_line.forEach { line -> mainLine.add(line.customer_name) }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.mainLine,
                            mainLine,
                            this@AddCustomerActivity
                        )
                    }
                    is AddCustomerStatus.CreateCustomer -> {
                        startActivity(Intent(this@AddCustomerActivity, MainActivity::class.java))
                        Log.d(
                            TAG,
                            "fetchData: ${it.data.status}"
                        )
                    }
                    is AddCustomerStatus.Error -> Log.d(TAG, "fetchData: ${it.error}")
                }
            }
        }
    }

    override fun onClick(v: View) {

        Log.d(
            TAG, versionName +
                    SharedPreferencesHelper.getInstance().getUserToken() +
                    customerTypePosition +
                    "Food" +
                    lineIdPosition +
                    mainCustomerLinePosition +
                    binding.customerName.text +
                    binding.customerAddress.text +
                    binding.customerNational.text
        )
        lifecycleScope.launch {
            viewModel.customerIntent.send(
                AddCustomerIntent.CreateCustomer(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    customerTypePosition,
                    "Food",
                    lineIdPosition,
                    mainCustomerLinePosition,
                    binding.customerName.text.toString(),
                    binding.customerAddress.text.toString(),
                    binding.customerNational.text.toString(),
                    latitude = latitude.toString(),
                    longitude = longitude.toString(),
                )
            )
        }

    }

    override fun onLocationChanged(location: Location) {
        if (!location.latitude.equals("")) {
            longitude = location.longitude
            latitude = location.latitude
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            binding.fieldLongitude.text = longitude.toString()
            binding.fieldLatitude.text= latitude.toString()
            Log.d(TAG, "longitude: ${location.longitude} + latitude: ${location.latitude}")
            getAddressFromLatLng(
                this@AddCustomerActivity,
                latitude,
                longitude
            ) { address ->
                Log.d(TAG, "getLocation: ${address}")
                binding.suggestedAddress.setText(address )
            }
        }

    }
    fun getAddressFromLatLng(
        context: Context,
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale("ar"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val fullAddress =
                                (0..address.maxAddressLineIndex).joinToString(", ") { index ->
                                    address.getAddressLine(index)
                                }
                            onResult(fullAddress)
                        } else {
                            onResult(null)
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        onResult(null)
                    }
                }
            )
        } else {
            // للأجهزة الأقل من API 33، لازم تستخدم النسخة القديمة (synchronous)
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullAddress =
                        (0..address.maxAddressLineIndex).joinToString(", ") { index ->
                            address.getAddressLine(index)
                        }
                    onResult(fullAddress)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }





}