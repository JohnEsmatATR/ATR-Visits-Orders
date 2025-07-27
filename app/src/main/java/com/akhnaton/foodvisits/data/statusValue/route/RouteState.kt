package com.akhnaton.foodvisits.data.statusValue.route

import com.google.android.gms.vision.barcode.Barcode
import org.osmdroid.util.GeoPoint

sealed class RouteState {
    object Idle : RouteState()
    object Loading : RouteState()
    data class Success(val geoPoints: List<GeoPoint>) : RouteState()
    data class Error(val message: String) : RouteState()
}
