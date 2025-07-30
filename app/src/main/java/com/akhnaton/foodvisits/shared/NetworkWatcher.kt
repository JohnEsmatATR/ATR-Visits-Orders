package com.akhnaton.foodvisits.shared

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class NetworkWatcher(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d("NetworkWatcher", "🔌 الإنترنت متاح، تشغيل Worker")

                val workRequest = OneTimeWorkRequestBuilder<TimeSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "TimeSyncWork",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d("NetworkWatcher", " فقد الاتصال بالإنترنت")
            }
        })
    }
}
