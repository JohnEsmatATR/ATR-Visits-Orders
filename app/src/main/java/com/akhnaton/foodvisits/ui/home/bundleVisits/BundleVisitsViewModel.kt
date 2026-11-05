package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.Manifest
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsStatus
import com.akhnaton.foodvisits.domin.BundleVisitsRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BundleVisitsViewModel(application: Application) : AndroidViewModel(application) {

    val intent = Channel<BundleVisitsIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<BundleVisitsStatus>(BundleVisitsStatus.Idle)
    val status: StateFlow<BundleVisitsStatus> get() = _status

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null

    init {
        handleIntents()
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intent.consumeAsFlow().collect {
                when (it) {
                    is BundleVisitsIntent.GetTodayVisits -> getTodayVisits()
                    is BundleVisitsIntent.SaveDone -> saveDone(it)
                    is BundleVisitsIntent.SaveReject -> saveReject(it.req)
                    is BundleVisitsIntent.RefreshToken -> refreshToken(it.userId, it.token)
                }
            }
        }
    }

    private fun getTodayVisits() {
        viewModelScope.launch {
            _status.value = BundleVisitsStatus.Loading
            _status.value = try {
                BundleVisitsStatus.GetTodayVisits(BundleVisitsRepository().getTodayVisits())
            } catch (e: Exception) {
                BundleVisitsStatus.Error(e.message)
            }
        }
    }

    private fun saveDone(req: BundleVisitsIntent.SaveDone) {
        viewModelScope.launch {
            _status.value = BundleVisitsStatus.Loading
            _status.value = try {
                BundleVisitsStatus.SaveDone(
                    BundleVisitsRepository().saveDone(
                        req.photo, req.visitId, req.status, req.latitude, req.longitude
                    )
                )
            } catch (e: Exception) {
                BundleVisitsStatus.Error(e.message)
            }
        }
    }

    private fun saveReject(req: BundleRejectReq) {
        viewModelScope.launch {
            _status.value = BundleVisitsStatus.Loading
            _status.value = try {
                BundleVisitsStatus.SaveReject(BundleVisitsRepository().saveReject(req))
            } catch (e: Exception) {
                BundleVisitsStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = BundleVisitsStatus.Loading
            _status.value = try {
                BundleVisitsStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                BundleVisitsStatus.Error(e.message)
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun startLocationUpdates() {
        stopLocationUpdates()
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(3_000L)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                _locationState.value = result.lastLocation ?: return
            }
        }
        fusedClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
        _locationState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}