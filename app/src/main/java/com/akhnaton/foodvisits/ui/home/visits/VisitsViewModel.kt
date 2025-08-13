package com.akhnaton.foodvisits.ui.home.visits

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.domin.VisitsRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VisitsViewModel(val context: Context) : ViewModel() {


    val visitsIntent = Channel<VisitsIntent>(Channel.UNLIMITED)

    private val _statusVisit = MutableStateFlow<VisitsStatus>(VisitsStatus.Idle)
    val statusVisit: StateFlow<VisitsStatus> get() = _statusVisit

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private lateinit var checkConnection: CheckConnection
    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    private val _timerState = MutableStateFlow("00:00:00")
    val timerState: StateFlow<String> = _timerState
    fun startTimer() {

        if (timerJob != null) return

        timerJob = viewModelScope.launch {
            while (isActive) {
                val hours = elapsedSeconds / 3600
                val minutes = (elapsedSeconds % 3600) / 60
                val seconds = elapsedSeconds % 60

                _timerState.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                delay(1000L)
                elapsedSeconds++
            }
        }
    }
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        stopTimer()
        elapsedSeconds = 0
        _timerState.value = "00:00:00"
    }
    init {
        getPlan()
    }

    private fun getPlan() {
        viewModelScope.launch {
            visitsIntent.consumeAsFlow().collect {
                when (it) {
                    is VisitsIntent.GetPlan -> fetchPlan(it.version, it.token)
                    is VisitsIntent.SaveVisit -> fetchSaveVisit(
                        it.version,
                        it.token,
                        it.customerPartySiteId,
                        it.visitType,
                        it.visitTarget,
                        it.visitActualTarget,
                        it.latitude,
                        it.longtitude,
                        it.startLat,
                        it.statLong,
                        it.deviceType,
                        it.zoneFlag,
                        it.checkInDate,
                        it.dateVisit,
                        it.customerType,
                        it.orderType,

                    )
                    is VisitsIntent.SaveVisitOnline -> saveVisitOnline()
                    is VisitsIntent.GetAppSetting -> getAppSetting(it.app_version)
                }
            }
        }
    }

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.Plan(VisitsRepository(context).getPlan(version, token))
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchSaveVisit(
        version: String,
        token: String,
        customerPartySiteId: String,
        visitType: String,
        visitTarget: String,
        visitActualTarget: String,
        latitude: String,
        longitude: String,
        startLat: String,
        startLong: String,
        deviceType: String,
        zoneFlag: String,
        checkInDate: String,
        dateVisit: String,
        customerType: String,
        orderType: String,
    ) {

        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                val result = VisitsRepository(context).saveVisit(
                    version,
                    token,
                    customerPartySiteId,
                    visitType,
                    visitTarget,
                    visitActualTarget,
                    latitude,
                    longitude,
                    startLong,
                    startLat,
                    deviceType,
                    zoneFlag,
                    checkInDate,
                    dateVisit,
                    customerType,
                    orderType,
                )

                checkConnection.deleteSaveVisitFromDB()

                VisitsStatus.SaveVisits(result)
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }


    private fun saveVisitOnline() {

        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.SaveVisitsOnline(
                    VisitsRepository(context).saveVisitOnline()
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }



    private fun getAppSetting(
        appVersion: String
    ) {
        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.GetAppSetting(
                    VisitsRepository(context).getAppSetting(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        )
            .setMinUpdateIntervalMillis(2_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    Log.d(
                        "Location",
                        "Lat: ${it.latitude}, Lon: ${it.longitude}, Accuracy: ${it.accuracy} meters"
                    )
                    _locationState.value = it
                } ?: run {
                    Log.e("Location", "Location is null")
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
            locationCallback = null

        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        stopLocationUpdates()
    }



    fun setElapsedSeconds(seconds: Int) {
        elapsedSeconds = seconds
    }

}