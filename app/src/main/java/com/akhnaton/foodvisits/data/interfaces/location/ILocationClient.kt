package com.akhnaton.foodvisits.data.interfaces.location

import android.location.Location
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.Flow

interface ILocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>
    fun checkGpsOpened(activity: ComponentActivity): Boolean

    class LocationException(message: String) : Exception()
}