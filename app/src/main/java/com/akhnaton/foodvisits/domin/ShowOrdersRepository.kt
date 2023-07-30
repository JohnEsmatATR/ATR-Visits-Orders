package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.apis.ISupervisor
import com.akhnaton.foodvisits.shared.RetrofitClient

class ShowOrdersRepository {
    private val retrofit = RetrofitClient.getInstance(ISupervisor::class.java)

    suspend fun getOrders(
        app_version: String?,
        api_token: String?,
        superId: String,
    ) = retrofit.getSuperOrders(app_version, api_token, superId)

    suspend fun rejectOrder(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
    ) = retrofit.rejectSuperOrder(
        app_version,
        api_token,
        orderNumber
    )

    suspend fun checkCreditLimit(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
    ) = retrofit.checkCreditLimit(
        app_version,
        api_token,
        orderNumber
    )

    suspend fun checkQouta(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
        super_id: String,
    ) = retrofit.checkQouta(
        app_version,
        api_token,
        orderNumber,
        super_id
    )

    suspend fun approveOrder(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
        super_id: String,
    ) = retrofit.approveOrder(
        app_version,
        api_token,
        orderNumber,
        super_id
    )
}