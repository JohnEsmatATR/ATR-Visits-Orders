package com.akhnaton.foodvisits.ui.home

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingIntent
import com.akhnaton.foodvisits.data.statusValue.appSetting.AppSettingStatus
import com.akhnaton.foodvisits.databinding.ActivityMainBinding
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.shared.GooeyMenu
import com.akhnaton.foodvisits.shared.NetworkWatcher
import com.akhnaton.foodvisits.shared.RealTimeService
import com.akhnaton.foodvisits.shared.SendVisitsWorker
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.customerCoding.CustomerCodingActivity
import com.akhnaton.foodvisits.ui.home.profile.ProfileActivity
import com.akhnaton.foodvisits.ui.home.supervisor.superShowOrders.SuperShowOrdersActivity
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModelFactory
import com.akhnaton.foodvisits.ui.home.visits.orderHistory.OrdersHistoryActivity
import com.akhnaton.foodvisits.ui.map.MapActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
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
    private val REQUESTLOCATION = 199
    private var requestPermission = RequestPermission()
    private var addCustomerEnable = false
    private val locationPermissionCode = 199

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        startSendVisitsWorker(this@MainActivity)
        NetworkWatcher(applicationContext).registerNetworkCallback()

        if (!isServiceRunning(RealTimeService::class.java)) {
            val serviceIntent = Intent(this, RealTimeService::class.java)
            startService(serviceIntent)
        }


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

        binding.profileBtn.setOnClickListener(this)
        binding.ordersHistoryBtn.setOnClickListener(this)
        binding.approvalBtn.setOnClickListener(this)
        binding.gooeyMenu.setOnMenuListener(this)
        binding.gooeyMenu.openCloseMenu(false)
        binding.mapBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, MapActivity::class.java))
        }


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
                            val serverTime = it.data.data.time
                            val mobileTime =
                                System.currentTimeMillis() / 1000

                            val diffInSecondsRaw = serverTime - mobileTime
                            val diffInSeconds = kotlin.math.abs(diffInSecondsRaw) // لازم قبل ما تقسمه
                            val diffMinutes = diffInSeconds / 60
                            val diffSeconds = diffInSeconds % 60
                            SharedPreferencesHelper.getInstance().saveTimeDifference(diffInSeconds)
                            Log.d("TAG", "Time Difference: -$diffMinutes minutes, $diffSeconds seconds")

                            Log.d("TAG", "Server Time: $serverTime")
                            Log.d("TAG", "Mobile Time: $mobileTime")
                            Log.d(
                                "TAG",
                                "Raw Difference: $diffInSecondsRaw seconds"
                            )
                            Log.d(
                                "TAG",
                                "Absolute Difference: $diffMinutes minutes, $diffSeconds seconds"
                            )

                        } catch (e: Exception) {
                            Log.e("TAG", "Error while comparing times: ${e.message}")
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
//                        Toast.makeText(
//                            this@MainActivity,
//                            "(FIRESTORE Retrieve Error) : $error",
//                            Toast.LENGTH_LONG
//                        ).show()
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
        enableLocation(this@MainActivity)
        requestPermission.permissionCheck(this)

    }


    override fun onDestroy() {
        super.onDestroy()
        requestPermission.stopServiceFunc(this)
    }

    fun enableLocation(activity: Activity) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000 // Interval = 30 seconds
        )
            .setMinUpdateIntervalMillis(5_000) // Fastest interval = 5 seconds
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are already satisfied
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, locationPermissionCode)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e(TAG, "Error starting resolution for location: ${sendEx.message}")
                }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUESTLOCATION -> when (resultCode) {
                Activity.RESULT_OK -> Log.d("abc", "OK")
                Activity.RESULT_CANCELED -> enableLocation(this@MainActivity)
            }
        }
    }

    private fun startSendVisitsWorker(context: Context) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


        val workRequest = OneTimeWorkRequestBuilder<SendVisitsWorker>()
            .setConstraints(constraints)
            .build()


        WorkManager.getInstance(context).enqueueUniqueWork(
            "SendVisitsWorker",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}