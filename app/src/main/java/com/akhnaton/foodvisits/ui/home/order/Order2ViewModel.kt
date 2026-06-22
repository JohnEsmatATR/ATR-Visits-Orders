package com.akhnaton.foodvisits.ui.home.order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.domin.Order2Repository
import com.akhnaton.foodvisits.domin.OrderRepository
import com.google.gson.JsonElement
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class Order2ViewModel : ViewModel() {


    val orderIntent = Channel<Order2Intent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<Order2Status>(Order2Status.Idle)

    val status: StateFlow<Order2Status> get() = _status

    init {
        getPaymentPlan()
    }

    private fun getPaymentPlan() {
        viewModelScope.launch {
            orderIntent.consumeAsFlow().collect {
                when (it) {
                    is Order2Intent.GetStartOrderData -> getStartOrderData(
                        it.partySiteId,
                        it.orderType,
                        it.customerCode
                    )
                    else -> {}
                }
            }
        }
    }

    private fun getStartOrderData(
        partySiteId: String,
        orderType: String,
        customerCode: String,
    ) {
        viewModelScope.launch {
            _status.value = Order2Status.Loading
            _status.value = try {
                Order2Status.GetStartOrderData(
                    Order2Repository().getStartOrderData(
                        partySiteId,
                        orderType,
                        customerCode
                    )
                )
            } catch (e: Exception) {
                Order2Status.Error(e.message)
            }
        }
    }

}