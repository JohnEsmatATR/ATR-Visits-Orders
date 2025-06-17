package com.akhnaton.foodvisits.shared

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SharedPreferencesHelper : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: SharedPreferencesHelper = SharedPreferencesHelper()

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        private val PREFS_NAME = "userInfo"


        @Synchronized
        public fun getInstance(): SharedPreferencesHelper {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

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

    fun setUserData(
        apiToken: String,
        username: String,
        employeeId: String,
        makeOrder: Boolean,
        prom: Boolean,
        telephone: Boolean,

    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("api_token", apiToken)
            .putString("user_name", username)
            .putString("employee_id", employeeId)
            .putBoolean("make_order", makeOrder)
            .putBoolean("prom", prom)
            .putBoolean("telephone", telephone).apply()

    }

    fun isLogged(): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("LOGGED", false)
    }

    fun setLogged() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("LOGGED", true).apply()
    }

    fun logOut() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("LOGGED", false).apply()
    }
}