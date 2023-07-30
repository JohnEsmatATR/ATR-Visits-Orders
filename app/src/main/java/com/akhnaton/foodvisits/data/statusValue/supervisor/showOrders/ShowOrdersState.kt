package com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders

import com.akhnaton.foodvisits.data.model.supervisor.showOrder.SuperStatus
import com.akhnaton.foodvisits.data.model.supervisor.StaticResponse


sealed class ShowOrdersState {

    object Idle : ShowOrdersState()
    object Loading : ShowOrdersState()
    data class ShowOrders(val superStatus: SuperStatus) : ShowOrdersState()
    data class RejectOrder(val superStatus: StaticResponse) : ShowOrdersState()
    data class CheckCreditLimit(val staticResponse: StaticResponse) : ShowOrdersState()
    data class CheckQouta(val staticResponse: StaticResponse) : ShowOrdersState()
    data class ApproveOrder(val staticResponse: StaticResponse) : ShowOrdersState()
    data class Error(val error: String?) : ShowOrdersState()
}