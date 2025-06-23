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
import androidx.biometric.BiometricPrompt
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
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

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
        binding.loginWithFingerPrint.setOnClickListener {
            showBiometricPrompt()
        }
       // requestNotificationPermission()


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
                            val username = binding.username.text.toString()
                            val password = binding.password.text.toString()
                            dialog.hide()
                            SharedPreferencesHelper().setLogged()
                            SharedPreferencesHelper().setUserData(
                                it.login.data.user.api_token,
                                it.login.data.user.user_name,
                                it.login.data.user.employee_id,
                                it.login.data.user.make_order,
                                it.login.data.user.prom,
                                it.login.data.user.telephone,

                            )

                                SharedPreferencesHelper().setLoginCredentials(username, password)


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
            loginIntent()
        }
    }

    private fun checkPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_COARSE_LOCATION)
        val readPhoneState = ContextCompat.checkSelfPermission(applicationContext, permission.READ_PHONE_STATE)

        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(applicationContext, permission.POST_NOTIFICATIONS)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        return fineLocation == PackageManager.PERMISSION_GRANTED &&
                coarseLocation == PackageManager.PERMISSION_GRANTED &&
                readPhoneState == PackageManager.PERMISSION_GRANTED &&
                notificationPermission == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermission() {
        val permissionsList = mutableListOf(
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.READ_PHONE_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(permission.POST_NOTIFICATIONS)
        }

        ActivityCompat.requestPermissions(
            this,
            permissionsList.toTypedArray(),
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
                val locationAccepted = grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED
                val coarseAccepted = grantResults.getOrNull(1) == PackageManager.PERMISSION_GRANTED
                val readPhoneAccepted = grantResults.getOrNull(2) == PackageManager.PERMISSION_GRANTED
                val notificationAccepted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    grantResults.getOrNull(3) == PackageManager.PERMISSION_GRANTED
                } else true

                if (locationAccepted && coarseAccepted && readPhoneAccepted && notificationAccepted) {
                    Toast.makeText(this, "Thanks For accepting Permissions", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Denied, You cannot access location data", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel("You need to allow access to all the permissions") { dialog, which ->
                            requestPermission()
                        }
                    }
                }
            }
        }
    }
    private fun loginIntent(){
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val firebaseToken = task.result
                val username = binding.username.text.toString().lowercase().trim()
                val password = binding.password.text.toString().trim()

                Log.d("FCM", ">>> Sending login data:")
                Log.d("FCM", "Version: $versionName")
                Log.d("FCM", "Username: $username")
                Log.d("FCM", "Password: $password")
                Log.d("FCM", "Firebase Token: $firebaseToken")

                lifecycleScope.launch {
                    viewModel.loginIntent.send(
                        LoginIntent.Login(
                            versionName,
                            username,
                            password,
                            firebaseToken
                        )
                    )
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

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    val (username, password) = SharedPreferencesHelper.getInstance().getLoginCredentials()
                    binding.username.setText(username)
                    binding.password.setText(password)
                    if (username != null && password != null) {
                        loginIntent()

                    } else {
                        binding.error.text = "لا يوجد بيانات محفوظة لتسجيل الدخول"
                    }
                }


                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    binding.error.text = "خطأ في البصمة: $errString"
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    binding.error.text = "البصمة غير صحيحة، حاول مرة أخرى"
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("تسجيل الدخول بالبصمة")
            .setSubtitle("قم بتأكيد هويتك باستخدام بصمة الإصبع")
            .setNegativeButtonText("إلغاء")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


}