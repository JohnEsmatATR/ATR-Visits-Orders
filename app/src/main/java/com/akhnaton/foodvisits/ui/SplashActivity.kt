package com.akhnaton.foodvisits.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.airbnb.lottie.LottieAnimationView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.BiometricActivity
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.getUserCredentials
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.saveUserCredentials
import com.akhnaton.foodvisits.shared.RealTimeService
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach)
        checkForAppUpdate()

        if (!isServiceRunning(RealTimeService::class.java)) {
            val serviceIntent = Intent(this, RealTimeService::class.java)
            startService(serviceIntent)
        }
        val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
        animationView.setAnimation("celularmaps.json")

//        animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                super.onAnimationEnd(animation)
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//                finishAffinity()
//            }
//        })

        Handler(Looper.getMainLooper()).postDelayed({
            val manufacturer = Build.MANUFACTURER.lowercase()
            val model = Build.MODEL.lowercase()

            Log.d("SplashCheck", "Manufacturer: $manufacturer, Model: $model")


            val isPOSDevice = manufacturer.contains("sunmi") || manufacturer.contains("pax") || manufacturer.contains("verifone")

            if (isPOSDevice) {

                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finishAffinity()
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val (username, password) = getUserCredentials(this@SplashActivity)
                    if (username != null && password != null) {
                        startActivity(Intent(this@SplashActivity, BiometricActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }, 3400)

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

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {

                finish()
            }
        }
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 100
    }

}