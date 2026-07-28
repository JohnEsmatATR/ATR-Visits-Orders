package com.akhnaton.foodvisits.ui.auth

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.login.LoginIntent
import com.akhnaton.foodvisits.data.statusValue.login.LoginState
import com.akhnaton.foodvisits.databinding.ActivityLogin2Binding
import com.akhnaton.foodvisits.databinding.ActivityLoginBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.EncryptedPrefsHelper.saveUserCredentials
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.devhoony.lottieproegressdialog.LottieProgressDialog
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlin.getValue

class LoginActivity2 : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLogin2Binding
    private val viewModel: LoginViewModel by viewModels()
    private val PERMISSION_REQUEST_CODE: Int = 200
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var deviceid1: String
    private lateinit var deviceid2: String
    private lateinit var dialog: LottieProgressDialog

    var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this@LoginActivity2, R.layout.activity_login2)

        @SuppressLint("HardwareIds") val myAndroidDeviceId = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )

        deviceid1 = myAndroidDeviceId
        deviceid2 = myAndroidDeviceId
        handleBackPress()
        if (!checkPermission()) requestPermission()

//        binding.appVersion.text = "App Version: $versionName"
        dialog = ProgressDialogHelper().showProgress(this@LoginActivity2)
        binding.btnLogin.setOnClickListener(this)
        makeLogin()

        binding.ivTogglePassword.setOnClickListener {

            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                binding.ivTogglePassword.setImageResource(
                    R.drawable.ic_eye_close
                )
            } else {
                binding.etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                binding.ivTogglePassword.setImageResource(
                    R.drawable.ic_eye_open
                )
            }

            binding.etPassword.setSelection(
                binding.etPassword.text?.length ?: 0
            )
        }

    }

    private fun setWarningUserName(): Boolean {
        if (binding.etUsername.text?.isEmpty() == true) {
            binding.etUsername.error = "Enter Username"
            binding.etUsername.requestFocus()
            return false
        }
        return true
    }

    private fun setWarningPassword(): Boolean {
        if (binding.etPassword.text?.isEmpty() == true) {
            binding.etPassword.error = "Enter Password"
            binding.etPassword.requestFocus()
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
                            val username = binding.etUsername.text.toString()
                            val password = binding.etPassword.text.toString()

                            var data = it.login.data.get(0)
                            dialog.hide()
                            SharedPreferencesHelper().setLogged()
                            SharedPreferencesHelper().setUserData(
                                data.TOKEN,
                                data.USER_NAME,
                                data.USER_ID,
                                true,
                                if (data.USER_CATEGORY == "prom") true else false,
                                true,
                                if (data.USER_CATEGORY == "super" || data.USER_CATEGORY == "gsuper") true else false,
                                data.ALLOWED_TO_MAKE_ORDER,
                                data.ALLOWED_TO_MAKE_RATE,
                                )
                            SharedPreferencesHelper().setDebugUsername(
                                binding.etUsername.text.toString()
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                saveUserCredentials(this@LoginActivity2, username, password)
                            }

                            Log.d(TAG, "makeLogin: " + data.USER_ID)
                            startActivity(Intent(this@LoginActivity2, MainActivity::class.java))
                            finishAffinity()
                        } else {
                            dialog.hide()
//                            binding.txtError.text = it.login.message
                            DialogUtils.showResultDialog(
                                context = this@LoginActivity2,
                                message = it.login.message,
                                isSuccess = false,
                                showOkButton = true
                            )
                        }
                    }

                    is LoginState.Error -> {
                        Log.d(TAG, "makeLogin Error: $it")
                        dialog.hide()
//                        val error = "Something went wrong"
//                        binding.txtError.text = error
                        DialogUtils.showResultDialog(
                            context = this@LoginActivity2,
                            message = it.error.toString(),
                            isSuccess = false,
                            showOkButton = true
                        )
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
        val fineLocation =
            ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_FINE_LOCATION)
        val coarseLocation =
            ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_COARSE_LOCATION)
        val readPhoneState =
            ContextCompat.checkSelfPermission(applicationContext, permission.READ_PHONE_STATE)

        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(applicationContext, permission.POST_NOTIFICATIONS)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED && readPhoneState == PackageManager.PERMISSION_GRANTED && notificationPermission == PackageManager.PERMISSION_GRANTED
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
            this, permissionsList.toTypedArray(), PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val locationAccepted =
                    grantResults.getOrNull(permissions.indexOf(permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
                val coarseAccepted =
                    grantResults.getOrNull(permissions.indexOf(permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED
                val readPhoneAccepted =
                    grantResults.getOrNull(permissions.indexOf(permission.READ_PHONE_STATE)) == PackageManager.PERMISSION_GRANTED
                val notificationAccepted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        grantResults.getOrNull(permissions.indexOf(permission.POST_NOTIFICATIONS)) == PackageManager.PERMISSION_GRANTED
                    } else true

                if (locationAccepted && coarseAccepted && readPhoneAccepted && notificationAccepted) {
                    Toast.makeText(this, "Thanks for accepting permissions", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (!notificationAccepted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(
                            this,
                            "You denied notification permission. App may not show notifications.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    Toast.makeText(
                        this,
                        "Permission Denied. You cannot access location data",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                            permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        showMessageOKCancel("You need to allow access to all the permissions") { dialog, _ ->
                            requestPermission()
                        }
                    }
                }
            }
        }
    }

    private fun loginIntent() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val firebaseToken = task.result
            val username = binding.etUsername.text.toString().lowercase().trim()
            val password = binding.etPassword.text.toString().trim()

            Log.d("FCM", ">>> Sending login data:")
            Log.d("FCM", "Version: $versionName")
            Log.d("FCM", "Username: $username")
            Log.d("FCM", "Password: $password")
            Log.d("FCM", "Firebase Token: $firebaseToken")

            lifecycleScope.launch {
                viewModel.loginIntent.send(
                    LoginIntent.Login(
                        username,
                        password,
                    )
                )

//
//                    val serviceIntent = Intent(this@LoginActivity2, RealTimeService::class.java)
//                    ContextCompat.startForegroundService(this@LoginActivity2, serviceIntent)
            }
        }.addOnFailureListener {
            Log.d("FCM", "Failed to get token", it)
        }
    }


    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@LoginActivity2).setMessage(message)
            .setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show()
    }


    companion object {
        private const val TAG = "LoginActivity2"
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this@LoginActivity2) {
            finishAffinity()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration)
        configuration.fontScale = 1.0f

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

}