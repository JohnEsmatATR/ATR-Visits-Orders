package com.akhnaton.foodvisits.shared

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

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

    fun setUserData(apiToken: String, username: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("api_token", apiToken)
            .putString("user_name", username).apply()
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