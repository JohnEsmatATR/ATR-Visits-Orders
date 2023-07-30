package com.akhnaton.foodvisits.ui.home.visits.order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.order.OrderIntent
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus
import com.akhnaton.foodvisits.domin.OrderRepository
import com.google.gson.JsonElement
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {


    val orderIntent = Channel<OrderIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<OrderStatus>(OrderStatus.Idle)

    val status: StateFlow<OrderStatus> get() = _status

    init {
        getPaymentPlan()
    }

    private fun getPaymentPlan() {
        viewModelScope.launch {
            orderIntent.consumeAsFlow().collect {
                when (it) {
                    is OrderIntent.GenerateOrderNumber -> generateOrderNumber(
                        it.app_version,
                        it.api_token,
                        it.customerPartySiteId,
                        it.orderType,
                        it.customerType,
                        it.paymentTermId,
                        it.visitId
                    )
                    is OrderIntent.GetCategories -> getCategories(
                        it.app_version,
                        it.api_token,
                        it.orderType,
                        it.customer_type
                    )
                    is OrderIntent.GetProducts -> getProducts(
                        it.app_version, it.api_token, it.orderType,
                        it.sub_category, it.customer_type, it.customer_code,
                        it.customer_party_site_id,  it.item_price_list,
                    )
                    is OrderIntent.SendOrder -> sendOrder(it.request)
                    is OrderIntent.SaveOrderPending -> saveOrderPending(it.request)
                    is OrderIntent.SavedOrder -> savedOrder(it.appVersion, it.apiToken, it.orderNumber)
                    is OrderIntent.GetOrderLimit -> getOrderLimit(it.app_version)
                }
            }
        }
    }

    private fun generateOrderNumber(
        appVersion: String,
        apiToken: String,
        customerPartySiteId: String,
        orderType: String,
        customerType: String,
        paymentTermId: String,
        visitId: String
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.GetOrderNumber(
                    OrderRepository().generateOrderNumber(
                        appVersion, apiToken,
                        customerPartySiteId, orderType,
                        customerType, paymentTermId, visitId
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }

    private fun getCategories(appVersion: String, apiToken: String, orderType: String, customer_type: String) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.GetCategories(
                    OrderRepository().getCategories(
                        appVersion,
                        apiToken,
                        orderType,
                        customer_type
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }


    private fun getProducts(
        appVersion: String,
        apiToken: String,
        orderType: String,
        subCategory: String,
        customerType: Int,
        customerCode: Int,
        customerPartySiteId: Int,
        item_price_list: String,
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.GetProducts(
                    OrderRepository().getProducts(
                        appVersion,
                        apiToken,
                        orderType,
                        subCategory,
                        customerType,
                        customerCode,
                        customerPartySiteId,
                        item_price_list
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }


    private fun getOrderLimit(
        appVersion: String
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.GetOrderLimit(
                    OrderRepository().getOrderLimit(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }


    private fun sendOrder(
        request: JsonElement,
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.SendOrder(
                    OrderRepository().sendOrder(
                        request
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }


    private fun saveOrderPending(
        request: JsonElement,
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.SaveOrderPending(
                    OrderRepository().saveOrderPending(
                        request
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }


    private fun savedOrder(
        appVersion: String,
        apiToken: String,
        orderNumber: String,
    ) {
        viewModelScope.launch {
            _status.value = OrderStatus.Idle
            _status.value = try {
                OrderStatus.SavedOrder(
                    OrderRepository().savedOrder(
                        appVersion,
                        apiToken,
                        orderNumber
                    )
                )
            } catch (e: Exception) {
                OrderStatus.Error(e.message)
            }
        }
    }



}