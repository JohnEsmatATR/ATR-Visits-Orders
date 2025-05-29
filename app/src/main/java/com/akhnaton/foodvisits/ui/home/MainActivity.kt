package com.akhnaton.foodvisits.ui.home

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingIntent
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingStatus
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityMainBinding
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.shared.GooeyMenu
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.customerCoding.CustomerCodingActivity
import com.akhnaton.foodvisits.ui.home.profile.ProfileActivity
import com.akhnaton.foodvisits.ui.home.supervisor.superShowOrders.SuperShowOrdersActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModelFactory
import com.akhnaton.foodvisits.ui.home.visits.orderHistory.OrdersHistoryActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener, GooeyMenu.GooeyMenuInterface {

    val TAG = "MainActivity"
    private lateinit var checkConnection: CheckConnection
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var visitViewModel: VisitsViewModel
    private var navHostFragment = NavHostFragment()
    private var googleApiClient: GoogleApiClient? = null
    private val REQUESTLOCATION = 199
    private var requestPermission = RequestPermission()
    private var addCustomerEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
    }

    private fun setupBinding() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        visitViewModel = ViewModelProvider(
            this,
            VisitsViewModelFactory(baseContext)
        )[VisitsViewModel::class.java]
        checkConnection = CheckConnection(baseContext)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.navView, navController)

//        FirebaseApp.initializeApp(this)
//        val firebaseAppCheck = FirebaseAppCheck.getInstance()
//        firebaseAppCheck.installAppCheckProviderFactory(
//            PlayIntegrityAppCheckProviderFactory.getInstance()
//        )

        binding.profileBtn.setOnClickListener(this)
        binding.ordersHistoryBtn.setOnClickListener(this)
        binding.approvalBtn.setOnClickListener(this)
        binding.gooeyMenu.setOnMenuListener(this)
        binding.gooeyMenu.openCloseMenu(false)


        lifecycleScope.launch {
            viewModel.mainIntent.send(AppSettingIntent.GetAppSetting(BuildConfig.VERSION_NAME))
        }

        if (!SharedPreferencesHelper.getInstance()
                .getMakeOrder() && !SharedPreferencesHelper.getInstance().getProm()
        ) {
            binding.approvalBtn.visibility = View.VISIBLE
        } else {
            binding.approvalBtn.visibility = View.GONE
        }

        fetchData()
        getProfileImage(binding)
    }

    private fun fetchDataVisit() {
        lifecycleScope.launch {
            visitViewModel.statusVisit.collect {
                when (it) {
                    is VisitsStatus.Idle -> Log.d(TAG, "fetchData: ")
                    is VisitsStatus.Loading -> Log.d(TAG, "fetchData: ")

                    is VisitsStatus.SaveVisitsOnline -> {
                        Log.d("jnjndjnjndjnjnd", "fetchData: ${it.data.data.visit_id}")
                        checkConnection.deleteSaveVisitFromDB()
                    }

                    is VisitsStatus.Error -> Log.d(TAG, "Error====== ${it.error}")
                    else -> {}
                }
            }
        }
    }

    private fun sendSaveVisits() {
        lifecycleScope.launch {
            visitViewModel.visitsIntent.send(
                VisitsIntent.SaveVisitOnline
            )
        }
    }


    override fun onStart() {
        super.onStart()
        if (!SharedPreferencesHelper().isLogged()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        } else {
            Log.d("TAG", "onStart: User Login Success")
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    is AppSettingStatus.Idle -> Log.d("TAG", "Idle: ")
                    is AppSettingStatus.Loading -> Log.d("TAG", "Loading: ")
                    is AppSettingStatus.GetAppSetting -> {
                        try {
                            Log.d(
                                "TAG",
                                "GetAppSetting: ${it.data.data.food_app_add_customer} "
                            )
                        } catch (e: Exception) {
                        }
                        try {
                            addCustomerEnable = it.data.data.food_app_add_customer
                        } catch (e: Exception) {
                        }
                    }

                    is AppSettingStatus.Error -> Log.d("TAG", "Error: ${it.error.toString()} ")
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.profileBtn.id -> {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            }

            binding.ordersHistoryBtn.id -> {
                startActivity(Intent(this@MainActivity, OrdersHistoryActivity::class.java))
            }

            binding.approvalBtn.id -> {
                startActivity(Intent(this@MainActivity, SuperShowOrdersActivity::class.java))
            }
        }
    }


    private fun getProfileImage(binding: ActivityMainBinding) {
        if (!SharedPreferencesHelper.getInstance().getUserToken().isNullOrEmpty()) {

            val db = Firebase.firestore
            db.collection("Users").document(SharedPreferencesHelper.getInstance().getUserToken())
                .get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {
                            val image = task.result.getString("image")
                            Log.d("TAG", "onFireStoreImage: $image")
                            Glide.with(applicationContext).load(image)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.profileImg)
                        }
                    } else {
                        val error = task.exception!!.message
                        Toast.makeText(
                            this@MainActivity,
                            "(FIRESTORE Retrieve Error) : $error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun menuOpen() {
    }

    override fun menuClose() {
    }

    override fun menuItemClicked(menuNumber: Int) {

        if (menuNumber == 1) {
//            if (addCustomerEnable) {
//                startActivity(Intent(this, AddCustomerActivity::class.java))
//            } else {
//                Toast.makeText(
//                    this@MainActivity,
//                    "غير متاحة حالياً",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
            startActivity(Intent(this, CustomerCodingActivity::class.java))

        }

        if (menuNumber == 2) {
            val nav = findNavController(navHostFragment)
            nav.navigateUp() || super.onSupportNavigateUp()

        }

        if (menuNumber == 3) {
            startActivity(Intent(this, WebOrderActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        enableLocation()
        requestPermission.permissionCheck(this)
        sendSaveVisits()
        fetchDataVisit()
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPermission.stopServiceFunc(this)
    }

    private fun enableLocation() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient?.connect()
                }
            })
            .addOnConnectionFailedListener {
            }.build()

        googleApiClient?.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient!!, builder.build())
        result.setResultCallback {
            val status: Status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(
                        this@MainActivity,
                        REQUESTLOCATION
                    )
                } catch (e: IntentSender.SendIntentException) {
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTLOCATION -> when (resultCode) {
                Activity.RESULT_OK -> Log.d("abc", "OK")
                Activity.RESULT_CANCELED -> enableLocation()
            }
        }
    }
}