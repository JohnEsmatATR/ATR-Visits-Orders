package com.akhnaton.foodvisits.shared.location

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.akhnaton.foodvisits.shared.Util
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority

class RequestPermission {

    private var mLocationService: GetLocationService = GetLocationService()
    private lateinit var mServiceIntent: Intent
    private val TAG = "RequestPermission"
    private val locationPermissionCode = 199

    companion object {
        const val MY_FINE_LOCATION_REQUEST = 99
        const val MY_BACKGROUND_LOCATION_REQUEST = 100
    }

    fun permissionCheck(context: Activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {

                    starServiceFunc(context)


                } else if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    starServiceFunc(context)
                }
            } else {
                starServiceFunc(context)
            }

        } else if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                requestFineLocationPermission(context)
            } else {
                requestFineLocationPermission(context)
            }
        }
    }

    private fun starServiceFunc(context: Activity) {
        mLocationService = GetLocationService()
        mServiceIntent = Intent(context, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, context)) {
            context.startService(mServiceIntent)
            Log.d(TAG, "Service Start:")
        } else {
            Log.d(TAG, "Service is already Start!!")
        }
    }

    fun stopServiceFunc(context: Activity) {
        mLocationService = GetLocationService()
        mServiceIntent = Intent(context, mLocationService.javaClass)
        if (Util.isMyServiceRunning(mLocationService.javaClass, context)) {
            context.stopService(mServiceIntent)
            Log.d(TAG, "Service stopped!!")
        } else {
            Log.d(TAG, "Service is already stopped!!")
        }
    }

    private fun requestFineLocationPermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_FINE_LOCATION_REQUEST
        )
    }

    private fun requestBackgroundLocationPermission(context: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                MY_BACKGROUND_LOCATION_REQUEST
            )
        }
    }

    fun enableLocation(activity: Activity) {


        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {

            Log.d("Location", "Location settings are satisfied.")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {

                    exception.startResolutionForResult(activity, locationPermissionCode)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("Location", "Error resolving location settings: ${sendEx.message}")
                }
            } else {
                Log.e("Location", "Location settings check failed: ${exception.message}")
            }
        }
    }
}