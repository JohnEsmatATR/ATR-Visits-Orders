package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IVisits
import com.akhnaton.foodvisits.shared.RetrofitClient

class VisitsRepository {
    private val retrofit = RetrofitClient.getInstance(IVisits::class.java)

    suspend fun getPlan(version: String, token: String) =
        retrofit.getPlan(version, token)


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
        phoneVisit:Boolean
    ) = retrofit.saveVisits(
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
        phoneVisit
    )

    suspend fun getAppSetting(version: String) = retrofit.getAppSetting(version)

}