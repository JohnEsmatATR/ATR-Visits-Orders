package com.akhnaton.foodvisits.shared

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

@RequiresApi(Build.VERSION_CODES.M)
object EncryptedPrefsHelper {

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveUserCredentials(context: Context, username: String, password: String) {
        val sharedPrefs = getEncryptedPrefs(context)
        with(sharedPrefs.edit()) {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun getUserCredentials(context: Context): Pair<String?, String?> {
        val sharedPrefs = getEncryptedPrefs(context)
        val username = sharedPrefs.getString("username", null)
        val password = sharedPrefs.getString("password", null)
        return Pair(username, password)
    }
    fun clearUserCredentials(context: Context) {
        val sharedPrefs = getEncryptedPrefs(context)
        sharedPrefs.edit().clear().apply()
    }
}
