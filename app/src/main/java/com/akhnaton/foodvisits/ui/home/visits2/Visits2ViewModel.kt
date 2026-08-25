package com.akhnaton.foodvisits.ui.home.visits2

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.checkInGPS.CheckInGPSReq
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.promoterSaveStock.PromoterSaveStockReq
import com.akhnaton.foodvisits.data.model.saveVisitGps.SaveVisitGpsReq
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.domin.CheckConnection
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import com.akhnaton.foodvisits.domin.Visits2Repository
import com.akhnaton.foodvisits.shared.getDistanceFromCurrentLocation
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

class Visits2ViewModel(val context: Context) : ViewModel() {

    val visitsIntent = Channel<Visits2Intent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<Visits2Status>(Visits2Status.Idle)

    val status: StateFlow<Visits2Status> get() = _status

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState.asStateFlow()

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    private val _distanceMeters = MutableStateFlow<Double?>(null)
    val distanceMeters = _distanceMeters.asStateFlow()

    private val _distanceKm = MutableStateFlow<Double?>(null)
    val distanceKm = _distanceKm.asStateFlow()

    init {
        getVisits()
    }

    private fun getVisits() {
        viewModelScope.launch {
            visitsIntent.consumeAsFlow().collect {
                when (it) {
                    is Visits2Intent.GetVisitPlan -> getVisitPlan()

                    is Visits2Intent.GetSalesMan -> getSalesMan()

                    is Visits2Intent.CopyDayPlan -> copyDayPlan(it.copyDayPlanReq)

                    is Visits2Intent.GetList -> getList(
                        it.page,
                        it.perPage,
                        it.status,
                        it.dateFrom,
                        it.dateTo,
                        it.search,
                        it.orderType
                    )

                    is Visits2Intent.CheckIn -> checkInGPS(
                        it.checkInGPSReq,
                    )

                    is Visits2Intent.SaveVisitGps -> saveVisitGps(
                        it.saveVisitGpsReq,
                    )

                    is Visits2Intent.DeleteOrder -> deleteOrder(
                        it.orderId,
                    )

                    is Visits2Intent.GetItems -> getItems(
                        it.orderId,
                    )

                    is Visits2Intent.VisitsSelect -> visitsSelect(it.orderType, it.customerCode)
                    is Visits2Intent.GetSalesAndCustomerTypes -> getSalesAndCustomerTypes()
                    is Visits2Intent.PromoterGetItemData -> promoterGetItemData(
                        it.customerCode,
                        it.partySiteId
                    )

                    is Visits2Intent.PromoterSaveStock -> promoterSaveStock(
                        it.promoterSaveStockReq
                    )

                    is Visits2Intent.RefreshToken -> refreshToken(it.userId, it.token)

                    else -> {}
                }
            }
        }
    }

    private fun getVisitPlan() {
        Log.d("WHAT", "getVisitPlanVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getVisitPlanVIEWMODEL1")
                Visits2Status.GetVisitPlan(
                    Visits2Repository().getVisitPlan()
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getVisitPlanVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun getSalesMan() {
        Log.d("WHAT", "getSalesManVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getSalesManVIEWMODEL1")
                Visits2Status.GetSalesMan(
                    Visits2Repository().getSalesMan()
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getSalesManVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun copyDayPlan(copyDayPlanReq: CopyDayPlanReq) {
        Log.d("WHAT", "copyDayPlanVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "copyDayPlanVIEWMODEL1")
                Visits2Status.CopyDayPlan(
                    Visits2Repository().copyDayPlan(copyDayPlanReq)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "copyDayPlanVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun checkInGPS(checkInGPSReq: CheckInGPSReq) {
        Log.d("WHAT", "checkInGPSReqVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "checkInGPSReqVIEWMODEL1")
                Visits2Status.CheckIn(
                    Visits2Repository().checkInGPS(checkInGPSReq)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "saveVisitGpsVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun saveVisitGps(saveVisitGpsReq: SaveVisitGpsReq) {
        Log.d("WHAT", "getCustomersVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getCustomersVIEWMODEL1")
                Visits2Status.SaveVisitGps(
                    Visits2Repository().saveVisitGps(saveVisitGpsReq)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "saveVisitGpsVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun visitsSelect(orderType: String, customerCode: String) {
        Log.d("WHAT", "visitsSelectVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "visitsSelectVIEWMODEL1")
                Visits2Status.VisitsSelect(
                    Visits2Repository().visitsSelect(orderType, customerCode)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "visitsSelectVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun getList(
        page: String,
        perPage: String,
        status: String,
        dateFrom: String,
        dateTo: String,
        search: String,
        orderType: String,
    ) {
        Log.d("WHAT", "getVisitPlanVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getVisitPlanVIEWMODEL1")
                Visits2Status.GetList(
                    Visits2Repository().getList(
                        page,
                        perPage,
                        status,
                        dateFrom,
                        dateTo,
                        search,
                        orderType
                    )
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getVisitPlanVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun deleteOrder(orderId: String) {
        Log.d("WHAT", "deleteOrderVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "deleteOrderVIEWMODEL1")
                Visits2Status.DeleteOrder(
                    Visits2Repository().deleteOrder(orderId)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "deleteOrderVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun getItems(orderId: String) {
        Log.d("WHAT", "getItemsVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getItemsVIEWMODEL1")
                Visits2Status.GetItems(
                    Visits2Repository().getItems(orderId)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getItemsVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun getSalesAndCustomerTypes() {
        Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL1")
                Visits2Status.GetSalesAndCustomerTypes(
                    Visits2Repository().getSalesAndCustomerTypes()
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun promoterGetItemData(customerCode: String, partySiteId: String) {
        Log.d("WHAT", "promoterGetItemDataVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "promoterGetItemDataVIEWMODEL1")
                Visits2Status.PromoterGetItemData(
                    Visits2Repository().promoterGetItemData(customerCode, partySiteId)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "promoterGetItemDataVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun promoterSaveStock(promoterSaveStockReq: PromoterSaveStockReq) {
        Log.d("WHAT", "promoterSaveStockVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "promoterSaveStockVIEWMODEL1")
                Visits2Status.PromoterSaveStock(
                    Visits2Repository().promoterSaveStock(promoterSaveStockReq)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "promoterSaveStockVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        Log.d("WHAT", "refreshTokenVIEWMODEL")
        viewModelScope.launch {
            _status.value = Visits2Status.Loading
            _status.value = try {
                Log.d("WHAT", "refreshTokenVIEWMODEL1")
                Visits2Status.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "refreshTokenVIEWMODEL2 ${e.message}")
                Visits2Status.Error(e.message)
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun getCurrentLocation(
        customerLatitude: Double?,
        customerLongitude: Double?
    ) {
        if (customerLatitude == null || customerLongitude == null) {
            Log.e("Location", "Customer coordinates are null")
            return
        }

        // Prevent multiple callbacks from being registered
        stopLocationUpdates()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        )
            .setMinUpdateIntervalMillis(3_000L)
            .setMaxUpdateDelayMillis(5_000L)
            .build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {

                val currentLocation = locationResult.lastLocation
                    ?: return

                Log.d(
                    "Location",
                    "Lat: ${currentLocation.latitude}, " +
                            "Lon: ${currentLocation.longitude}, " +
                            "Accuracy: ${currentLocation.accuracy}"
                )

                _locationState.value = currentLocation

                val distanceKm = getDistanceFromCurrentLocation(
                    currentLocation = currentLocation,
                    targetLat = customerLatitude,
                    targetLng = customerLongitude
                )

                _distanceKm.value = distanceKm
                _distanceMeters.value = distanceKm * 1000.0

                Log.d(
                    "Distance",
                    "Distance: %.2f KM / %.2f meters"
                        .format(
                            distanceKm,
                            distanceKm * 1000.0
                        )
                )
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

        _locationState.value = null
        _distanceMeters.value = null
        _distanceKm.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

}


