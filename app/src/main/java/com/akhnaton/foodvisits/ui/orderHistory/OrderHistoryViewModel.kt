package com.akhnaton.foodvisits.ui.orderHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryIntent
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryState
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.domin.OrderHistoryRepository
import com.akhnaton.foodvisits.domin.VisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class OrderHistoryViewModel : ViewModel() {


    val ordersIntent = Channel<OrderHistoryIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<OrderHistoryState>(OrderHistoryState.Idle)

    val status: StateFlow<OrderHistoryState> get() = _status

    init {
        getOrdersHistory()
    }

    private fun getOrdersHistory() {
        viewModelScope.launch {
            ordersIntent.consumeAsFlow().collect {
                when (it) {
                    is OrderHistoryIntent.OrderHistory -> fetchOrdersHistory(
                        token = it.token,
                        version = it.version,
                        to = it.to,
                        from = it.from
                    )
                    is OrderHistoryIntent.OrderHistoryDetails -> fetchOrderHistoryDetails(
                        it.token,
                        it.version,
                        it.orderNumber
                    )
                }
            }
        }
    }


    private fun fetchOrdersHistory(token: String, version: String, to: String, from: String) {
        viewModelScope.launch {
            _status.value = OrderHistoryState.Idle
            _status.value = try {
                OrderHistoryState.GetOrdersHistory(
                    OrderHistoryRepository().getOrdersHistory(
                        token = token,
                        version = version,
                        to = to,
                        from = from
                    )
                )
            } catch (e: Exception) {
                OrderHistoryState.Error(e.message)
            }
        }
    }

    private fun fetchOrderHistoryDetails(token: String, version: String, orderNumber: String) {
        viewModelScope.launch {
            _status.value = OrderHistoryState.Idle
            _status.value = try {
                OrderHistoryState.GetOrdersHistoryDetails(
                    OrderHistoryRepository().getOrderHistoryDetails(token, version, orderNumber)
                )
            } catch (e: Exception) {
                OrderHistoryState.Error(e.message)
            }
        }
    }

}