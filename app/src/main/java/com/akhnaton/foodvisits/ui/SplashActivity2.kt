package com.akhnaton.foodvisits.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.getUserCredentials
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity

class SplashActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash2)
        Handler(Looper.getMainLooper()).postDelayed({
            val manufacturer = Build.MANUFACTURER.lowercase()
            val model = Build.MODEL.lowercase()
            Log.d("SplashCheck", "Manufacturer: $manufacturer, Model: $model")
            val isPOSDevice =
                manufacturer.contains("sunmi") || manufacturer.contains("pax") || manufacturer.contains(
                    "verifone"
                )
            if (isPOSDevice) {
                startActivity(Intent(this@SplashActivity2, MainActivity::class.java))
                finishAffinity()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val (username, password) = getUserCredentials(this@SplashActivity2)
                    if (username != null && password != null) {
                        startActivity(Intent(this@SplashActivity2, MainActivity::class.java))
                        finishAffinity()
                    } else {
//                        startActivity(Intent(this@SplashActivity2, LoginActivity::class.java))
                        startActivity(Intent(this@SplashActivity2, LoginActivity2::class.java))
                        finishAffinity()
                    }
                }
            }
        }, 3400)
    }
}