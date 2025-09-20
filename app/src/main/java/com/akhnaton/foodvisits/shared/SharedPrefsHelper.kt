package com.akhnaton.foodvisits.shared

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsHelper {
    private const val PREF_NAME = "real_time_prefs"
    private const val KEY_SERVER_TIME = "server_time"
    private const val KEY_SERVER_TIME_STR = "server_time_str"
    private const val KEY_SERVER_TIME_SECONDS = "server_time_seconds"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveServerTime(context: Context, time: String) {
        getPrefs(context).edit().putString(KEY_SERVER_TIME, time).apply()
    }

    fun saveServerUnixTime(context: Context, timeSeconds: Long) {
        getPrefs(context).edit().putLong(KEY_SERVER_TIME_SECONDS, timeSeconds).apply()
    }

    fun getServerUnixTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_SERVER_TIME_SECONDS, 0L)
    }

    fun isServerTimeSaved(context: Context): Boolean {
        return getPrefs(context).contains(KEY_SERVER_TIME_SECONDS)
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }



}
