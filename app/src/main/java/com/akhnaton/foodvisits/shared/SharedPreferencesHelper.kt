package com.akhnaton.foodvisits.shared

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.core.content.edit
import com.akhnaton.foodvisits.shared.debugBanner.DebugBannerManager

class SharedPreferencesHelper : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: SharedPreferencesHelper = SharedPreferencesHelper()

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        private val PREFS_NAME = "userInfo"
        private const val KEY_TIME_DIFFERENCE = "time_difference"


        @Synchronized
        public fun getInstance(): SharedPreferencesHelper {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?
                ) {
                }

                override fun onActivityDestroyed(
                    activity: Activity
                ) {
                }

                override fun onActivityPaused(
                    activity: Activity
                ) {
                }

                override fun onActivityResumed(
                    activity: Activity
                ) {
                }

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle
                ) {
                }

                override fun onActivityStarted(
                    activity: Activity
                ) {
                    DebugBannerManager.show(activity)
                }

                override fun onActivityStopped(
                    activity: Activity
                ) {
                }
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    fun saveLong(key: String, value: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(key, value)
            .apply()
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(key, default)
    }

    fun saveTimeDifference(diffInSeconds: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putLong(KEY_TIME_DIFFERENCE, diffInSeconds)
            }
    }

    fun getTimeDifference(): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_TIME_DIFFERENCE, 0L)
    }

    fun getAppContext(): Context {
        return context
    }

    fun getUserToken(): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("api_token", "").toString()
    }

    fun getUsername(): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("user_name", "").toString()
    }

    fun setDebugUsername(username: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("debug_user_name", username)
            .apply()
    }

    fun getDebugUsername(): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("debug_user_name", "").toString()
    }

    fun getEmployeeId(): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("employee_id", "").toString()
    }

    fun getMakeOrder(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("make_order", false)
    }

    fun getProm(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("prom", false)
    }

    fun getTelephone(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("telephone", false)
    }

    fun setLoginCredentials(username: String, password: String) {
        Log.d("SharedPrefs", "Saving username: $username, password: $password")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("login_username", username)
            .putString("login_password", password)
            .apply()
    }


    fun getLoginCredentials(): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString("login_username", null)
        val password = prefs.getString("login_password", null)
        Log.d("SharedPrefs", "Retrieved username: $username, password: $password")
        return Pair(username, password)
    }


    fun setUserData(
        apiToken: String,
        username: String,
        employeeId: String,
        makeOrder: Boolean,
        isProm: Boolean,
        telephone: Boolean,
        isSuper: Boolean,
        allowedToMakeOrder: Boolean,
        allowedToMakeRate: Boolean,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("api_token", apiToken)
            .putString("user_name", username)
            .putString("employee_id", employeeId)
            .putBoolean("make_order", makeOrder)
            .putBoolean("is_prom", isProm)
            .putBoolean("telephone", telephone)
            .putBoolean("is_super", isSuper)
            .putBoolean("allowed_to_make_order", allowedToMakeOrder)
            .putBoolean("allowed_to_make_rate", allowedToMakeRate)
            .apply()
    }

    fun isLogged(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("LOGGED", false)
    }

    fun setLogged() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("LOGGED", true).apply()
    }

    fun saveUserToken(token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("api_token", token).apply()
    }

    fun logOut() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("LOGGED", false).apply()
    }

    fun isSuper(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("is_super", false)
    }

    fun isAllowedToMakeOrder(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("allowed_to_make_order", false)
    }

    fun isAllowedToMakeRate(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("allowed_to_make_rate", false)
    }


}