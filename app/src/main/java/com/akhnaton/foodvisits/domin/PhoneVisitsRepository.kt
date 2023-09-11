package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.IPhoneVisits
import com.akhnaton.foodvisits.shared.RetrofitClient

class PhoneVisitsRepository {
    private val retrofit = RetrofitClient.getInstance(IPhoneVisits::class.java)

    suspend fun getPlan(version: String, token: String) =
        retrofit.getPlan(version, token)


    suspend fun getCustomerType(version: String, token: String) =
        retrofit.getCustomerType(version, token)

    suspend fun getLines(version: String, token: String, customerType: String, orderType: String) =
        retrofit.getLines(version, token, customerType, orderType)

    suspend fun getMainLineCustomer(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String
    ) =
        retrofit.getMainLineCustomer(version, token, customerType, orderType, lineId)

    suspend fun getCustomersSite(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String,
        customer_code: String
    ) = retrofit.getCustomersSite(version, token, customerType, orderType, lineId, customer_code)

    suspend fun getAppSetting(version: String) = retrofit.getAppSetting(version)


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
        phoneVisit: Boolean,
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
        customerType,
        orderType,
        phoneVisit,
    )


}