package com.akhnaton.foodvisits.domin

import android.content.Context
import android.util.Log
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB
import com.akhnaton.foodvisits.data.interfaces.IVisits
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit
import com.akhnaton.foodvisits.shared.RetrofitClient

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
    )


    suspend fun saveVisitOnline() = checkConnection.saveVisitOnline()

    suspend fun getAppSetting(version: String) = checkConnection.getAppSetting(version)

}