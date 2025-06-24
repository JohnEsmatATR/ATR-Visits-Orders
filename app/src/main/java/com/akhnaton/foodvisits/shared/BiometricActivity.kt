package com.akhnaton.foodvisits.shared

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.ui.home.MainActivity


class BiometricActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        BiometricAuthHelper(this).authenticate(
            onSuccess = {
                startActivity(Intent(this@BiometricActivity,MainActivity::class.java))
                finish()
            },
            onError = {
                finishAffinity()
            }
        )
    }
}

