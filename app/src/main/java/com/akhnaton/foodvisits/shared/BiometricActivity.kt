package com.akhnaton.foodvisits.shared

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.akhnaton.foodvisits.databinding.ActivityBiometricBinding
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.clearUserCredentials
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.getUserCredentials
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.profile.ProfileActivity


class BiometricActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiometricBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiometricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authWithFingerPrint()

        binding.loginButton.setOnClickListener {
            loginPassword()
        }
        binding.loginWithFingerPrint.setOnClickListener {
            authWithFingerPrint()
        }
        logOutBtn()
        binding.logout.setOnClickListener {
            logoutFun()
        }

    }
    private fun authWithFingerPrint(){
        BiometricAuthHelper(this).authenticate(
            onSuccess = {
                startActivity(Intent(this@BiometricActivity,MainActivity::class.java))
                finish()
            },
            onError = {
                return@authenticate
            }
        )
    }

    private fun loginPassword(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val passwordEntered = binding.password.text.toString()
            val (_, password) =  getUserCredentials(this@BiometricActivity)
            val userPassword = password
            if (passwordEntered == userPassword){
                startActivity(Intent(this@BiometricActivity,MainActivity::class.java))
                finish()
            }else{
                Log.d("TAG", "loginPassword: passwordEntered : ${passwordEntered}  , userPassword : ${userPassword}")
                Toast.makeText(this@BiometricActivity,"كلمه المرور خطاء", Toast.LENGTH_LONG).show()
            }


        }
    }
    private fun logOutBtn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val (name, _) =  getUserCredentials(this@BiometricActivity)
            binding.logout.text="  انت لست ${name}"
            binding.textView3.text= " ادخل كلمه المرور الخاصه بحسابك  : ${name}"
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun logoutFun(){
        SharedPreferencesHelper.getInstance().logOut()
        clearUserCredentials(this@BiometricActivity)
        startActivity(Intent(this@BiometricActivity, LoginActivity::class.java))
        finishAffinity()
    }
}

