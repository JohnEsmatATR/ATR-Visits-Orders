package com.akhnaton.foodvisits.data.statusValue.orderHistory

import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistory
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistoryDetails

sealed class OrderHistoryState {

    object Idle : OrderHistoryState()
    object Loading : OrderHistoryState()
    data class GetOrdersHistory(val orders: OrderHistory) : OrderHistoryState()
    data class GetOrdersHistoryDetails(val orders: OrderHistoryDetails) : OrderHistoryState()
    data class Error(val error: String?) : OrderHistoryState()
}