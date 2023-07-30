package com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails

import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrder


sealed class OrderDetailsState {

    object Idle : OrderDetailsState()
    object Loading : OrderDetailsState()
    data class OrderDetails(val superOrder: SuperOrder) : OrderDetailsState()
    data class Error(val error: String?) : OrderDetailsState()
}