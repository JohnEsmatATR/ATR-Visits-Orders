package com.akhnaton.foodvisits.ui.home.order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.editOrder.EditOrderReq
import com.akhnaton.foodvisits.data.model.saveOrder.SaveOrderReq
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Intent
import com.akhnaton.foodvisits.data.statusValue.order2.Order2Status
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.domin.Order2Repository
import com.akhnaton.foodvisits.domin.OrderRepository
import com.akhnaton.foodvisits.domin.Visits2Repository
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

                    is Order2Intent.SaveOrder -> saveOrder(
                        it.saveOrderReq
                    )

                    is Order2Intent.GetItems -> getItems(
                        it.orderId,
                    )

                    is Order2Intent.EditOrder -> editOrder(
                        it.editOrderReq,
                    )

                    is Order2Intent.GetItemDetails -> getItemDetails(
                        it.itemId, it.priceList, it.storeId
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

    private fun saveOrder(
        saveOrderReq: SaveOrderReq
    ) {
        viewModelScope.launch {
            _status.value = Order2Status.Loading
            _status.value = try {
                Order2Status.SaveOrder(
                    Order2Repository().saveOrder(
                        saveOrderReq
                    )
                )
            } catch (e: Exception) {
                Order2Status.Error(e.message)
            }
        }
    }

    private fun getItems(orderId: String) {
        Log.d("WHAT", "getItemsVIEWMODEL")
        viewModelScope.launch {
            _status.value = Order2Status.Loading
            _status.value = try {
                Log.d("WHAT", "getItemsVIEWMODEL1")
                Order2Status.GetItems(
                    Order2Repository().getItems(orderId)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getItemsVIEWMODEL2 ${e.message}")
                Order2Status.Error(e.message)
            }
        }
    }

    private fun editOrder(
        editOrderReq: EditOrderReq
    ) {
        viewModelScope.launch {
            _status.value = Order2Status.Loading
            _status.value = try {
                Order2Status.EditOrder(
                    Order2Repository().editOrder(
                        editOrderReq
                    )
                )
            } catch (e: Exception) {
                Order2Status.Error(e.message)
            }
        }
    }

    private fun getItemDetails(
        itemId: String,
        priceList: String,
        storeId: String,
    ) {
        viewModelScope.launch {
            _status.value = Order2Status.Loading
            _status.value = try {
                Order2Status.GetItemDetails(
                    Order2Repository().getItemDetails(
                        itemId, priceList, storeId
                    )
                )
            } catch (e: Exception) {
                Order2Status.Error(e.message)
            }
        }
    }

}