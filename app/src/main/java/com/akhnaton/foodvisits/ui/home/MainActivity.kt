package com.akhnaton.foodvisits.ui.home

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.shared.GooeyMenu
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingIntent
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingStatus
import com.akhnaton.foodvisits.databinding.ActivityMainBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.home.addCustomer.AddCustomerActivity
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.visits.orderHistory.OrdersHistoryActivity
import com.akhnaton.foodvisits.ui.home.profile.ProfileActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener, GooeyMenu.GooeyMenuInterface {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
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

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.navView, navController)

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        binding.profileBtn.setOnClickListener(this)
        binding.ordersHistoryBtn.setOnClickListener(this)
        binding.gooeyMenu.setOnMenuListener(this)

        lifecycleScope.launch {
            viewModel.mainIntent.send(AppSettingIntent.GetAppSetting(BuildConfig.VERSION_NAME))
        }
        fetchData()
        getProfileImage(binding)
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
                        Log.d(
                            "TAG",
                            "GetAppSetting: ${it.data.data.food_app_add_customer} "
                        )
                        addCustomerEnable = it.data.data.food_app_add_customer
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
            if (addCustomerEnable) {
                startActivity(Intent(this, AddCustomerActivity::class.java))
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "غير متاحة حالياً",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (menuNumber == 2) {
            startActivity(Intent(this, WebOrderActivity::class.java))
        }

        if (menuNumber == 3) {
            val nav = findNavController(navHostFragment)
            nav.navigateUp() || super.onSupportNavigateUp()
        }

    }

    override fun onResume() {
        super.onResume()
        enableLocation()
        requestPermission.permissionCheck(this)
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