package com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.domin.OrderDetailsSuperRepository
import com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails.OrderDetailsIntent
import com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails.OrderDetailsState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class OrderDetailsViewModel : ViewModel() {

    val orderDetailsIntent = Channel<OrderDetailsIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<OrderDetailsState>(OrderDetailsState.Idle)
    val state: StateFlow<OrderDetailsState> get() = _state

    init {
        orderDetails()
    }


    private fun orderDetails() {
        viewModelScope.launch {
            orderDetailsIntent.consumeAsFlow().collect {
                when (it) {
                    is OrderDetailsIntent.GetOrderDetails -> orderDetailsRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                        it.superId,
                        it.order_total_price,
                        it.customer_id,
                    )
                }
            }
        }
    }

    private fun orderDetailsRepo(
        app_version: String,
        api_token: String,
        orderNumber: String,
        superId: String,
        order_total_price: String,
        customer_id: String,
    ) {
        viewModelScope.launch {
            _state.value = OrderDetailsState.Loading
            _state.value = try {
                OrderDetailsState.OrderDetails(
                    OrderDetailsSuperRepository().orderDetailsSuper(
                        app_version,
                        api_token,
                        orderNumber,
                        superId,
                        order_total_price,
                        customer_id
                    )!!
                )
            } catch (e: Exception) {
                OrderDetailsState.Error(e.message)
            }
        }
    }

}