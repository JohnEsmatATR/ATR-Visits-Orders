package com.akhnaton.foodvisits.domin

import android.content.Context

class VisitsRepository(context: Context) {
    private val checkConnection = CheckConnection(context)

    suspend fun getPlan(version: String, token: String) =
        checkConnection.getPlan(version, token)


    suspend fun saveVisit(
        version: String,
        token: String,
        customerPartySiteId: String,
        visitType: String,
        visitTarget: String,
        visitActualTarget: String,
        latitude: String,
        longitude: String,
        deviceType: String,
        zoneFlag: String,
        checkInDate: String,
        dateVisit: String,
        customerType: String,
        orderType: String,
    ) = checkConnection.saveVisit(
        version,
        token,
        customerPartySiteId,
        visitType,
        visitTarget,
        visitActualTarget,
        latitude,
        longitude,
        deviceType,
        zoneFlag,
        checkInDate,
        dateVisit,
        customerType,
        orderType,
    )


    suspend fun saveVisitOnline() = checkConnection.saveVisitOnline()

    suspend fun getAppSetting(version: String) = checkConnection.getAppSetting(version)

}