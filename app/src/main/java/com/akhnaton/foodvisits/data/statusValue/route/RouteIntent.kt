package com.akhnaton.foodvisits.data.statusValue.route


import org.osmdroid.util.GeoPoint

sealed class RouteIntent {
    data class FetchRoute(val waypoints: List<GeoPoint>) : RouteIntent()
}
