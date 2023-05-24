package com.akhnaton.foodvisits.ui.auth

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.login.LoginIntent
import com.akhnaton.foodvisits.data.statusValue.login.LoginState
import com.akhnaton.foodvisits.databinding.ActivityLoginBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.devhoony.lottieproegressdialog.LottieProgressDialog
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val PERMISSION_REQUEST_CODE: Int = 200
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var deviceid1: String
    private lateinit var deviceid2: String
    private lateinit var dialog: LottieProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@LoginActivity, R.layout.activity_login)

        @SuppressLint("HardwareIds")
        val myAndroidDeviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        deviceid1 = myAndroidDeviceId
        deviceid2 = myAndroidDeviceId

        if (!checkPermission()) requestPermission()

        binding.appVersion.text = "App Version: $versionName"
        dialog = ProgressDialogHelper().showProgress(this@LoginActivity)
        binding.loginButton.setOnClickListener(this)
        makeLogin()

    }

    private fun setWarningUserName(): Boolean {
        if (binding.username.text?.isEmpty() == true) {
            binding.username.error = "Enter Username"
            binding.username.requestFocus()
            return false
        }
        return true
    }

    private fun setWarningPassword(): Boolean {
        if (binding.password.text?.isEmpty() == true) {
            binding.password.error = "Enter Password"
            binding.password.requestFocus()
            return false
        }
        return true
    }

    private fun makeLogin() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {

                    is LoginState.Idle -> Log.d(TAG, "makeLogin: $it")
                    is LoginState.Loading -> dialog.show()

                    is LoginState.LogIn -> {
                        if (it.login.status != 400) {
                            dialog.hide()
                            SharedPreferencesHelper().setLogged()
                            SharedPreferencesHelper().setUserData(
                                it.login.data.user.api_token,
                                it.login.data.user.user_name,
                                it.login.data.user.employee_id,
                                it.login.data.user.make_order,
                                it.login.data.user.prom
                            )
                            Log.d(TAG, "makeLogin: " + it.login.data.user.employee_id)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finishAffinity()
                        } else {
                            dialog.hide()
                            binding.error.text = it.login.message
                        }
                    }

                    is LoginState.Error -> {
                        Log.d(TAG, "makeLogin Error: $it")
                        dialog.hide()
                        val error = "Something went wrong"
                        binding.error.text = error
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {

        if (setWarningUserName() && setWarningPassword()) {

            lifecycleScope.launch {
                viewModel.loginIntent.send(
                    LoginIntent.Login(
                        versionName,
                        binding.username.text.toString().lowercase().trim(),
                        binding.password.text.toString().trim(),
                    )
                )

            }
        }
    }

    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_FINE_LOCATION)
        val result1 =
            ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_COARSE_LOCATION)
        val result2 =
            ContextCompat.checkSelfPermission(applicationContext, permission.READ_PHONE_STATE)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_COARSE_LOCATION,
                permission.READ_PHONE_STATE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val read_phone_IMEI = grantResults[2] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted && cameraAccepted && read_phone_IMEI) Toast.makeText(
                    this,
                    "Thanks For accepting Permissions ",
                    Toast.LENGTH_SHORT
                ).show() else {
                    Toast.makeText(
                        this,
                        "Permission Denied, You cannot access location data ",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel(
                                "You need to allow access to both the permissions"
                            ) { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(
                                        arrayOf(
                                            permission.ACCESS_FINE_LOCATION,
                                            permission.ACCESS_COARSE_LOCATION,
                                            permission.READ_PHONE_STATE
                                        ),
                                        PERMISSION_REQUEST_CODE
                                    )
                                }
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@LoginActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


    companion object {
        private const val TAG = "LoginActivity"
    }

}