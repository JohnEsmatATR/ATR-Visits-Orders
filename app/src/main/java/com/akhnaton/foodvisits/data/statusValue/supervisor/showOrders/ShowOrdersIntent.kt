package com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders

sealed class ShowOrdersIntent {

    data class GetOrders(
        val app_version: String,
        val api_token: String,
        val superId: String,
    ) : ShowOrdersIntent()

    data class RejectOrder(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
    ) : ShowOrdersIntent()

    data class CheckCreditLimit(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
    ) : ShowOrdersIntent()

    data class CheckQouta(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
        val superId: String,
    ) : ShowOrdersIntent()

    data class ApproveOrder(
        val app_version: String,
        val api_token: String,
        val orderNumber: String,
        val superId: String,
    ) : ShowOrdersIntent()

}