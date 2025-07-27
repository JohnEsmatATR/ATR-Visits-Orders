package com.akhnaton.foodvisits.data.statusValue.map_state

import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.google.android.gms.vision.barcode.Barcode

// MapIntent.kt
sealed interface MapIntent {
    data object Initialize : MapIntent
    data class LocationUpdated(val geoPoint: Barcode.GeoPoint) : MapIntent
    data class FindRouteClicked(val customerId: String? = null) : MapIntent
    data class CustomerSelected(val customer: CustomerVisitPlan) : MapIntent
}