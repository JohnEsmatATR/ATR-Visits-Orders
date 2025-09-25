package com.akhnaton.foodvisits.ui

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
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach)


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
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                }
            }
        }, 3400)

    }
}