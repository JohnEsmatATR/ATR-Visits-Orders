package com.akhnaton.foodvisits.data.statusValue.map_state

import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.vision.barcode.Barcode


sealed interface MapState {
    data object Idle : MapState
    data object Loading : MapState
    data class Ready(
        val userLocation: Barcode.GeoPoint,
        val customers: List<CustomerMarker>,
        val route: Polyline?
    ) : MapState

    data class Error(val message: String) : MapState
}

data class CustomerMarker(
    val customer: CustomerVisitPlan,
    val marker: Marker,
    val routeOrder: Int
)