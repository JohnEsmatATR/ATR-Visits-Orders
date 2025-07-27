package com.akhnaton.foodvisits.data.statusValue.route

import com.google.android.gms.vision.barcode.Barcode
import org.osmdroid.util.GeoPoint

sealed class RouteIntent {
    data class FetchRoute(val waypoints: List<GeoPoint>) : RouteIntent()
}
