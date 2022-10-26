package com.akhnaton.foodvisits.shared.location

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.shared.Util
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

class RequestPermission {

    private var mLocationService: GetLocationService = GetLocationService()
    private lateinit var mServiceIntent: Intent
    private val TAG = "RequestPermission"
    private var googleApiClient: GoogleApiClient? = null
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

    fun enableLocation(context: Activity) {
        googleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient?.connect()
                }
            })
            .addOnConnectionFailedListener {
            }.build()

        googleApiClient?.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient!!, builder.build())
        result.setResultCallback {
            val status: Status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(
                        context,
                        locationPermissionCode
                    )
                } catch (e: IntentSender.SendIntentException) {
                }
            }
        }
    }
}