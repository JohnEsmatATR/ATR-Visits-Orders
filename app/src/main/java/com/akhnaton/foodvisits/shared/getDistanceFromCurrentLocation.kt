package com.akhnaton.foodvisits.shared

import android.location.Location

fun getDistanceFromCurrentLocation(
    currentLocation: Location,
    targetLat: Double,
    targetLng: Double
): Double {

    val targetLocation = Location("").apply {
        latitude = targetLat
        longitude = targetLng
    }

    return currentLocation.distanceTo(targetLocation) / 1000.0
}