package com.akhnaton.foodvisits.ui.home.supervisor.superShowOrders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.domin.ShowOrdersRepository
import com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders.ShowOrdersIntent
import com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders.ShowOrdersState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {

    val showOrdersIntent = Channel<ShowOrdersIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<ShowOrdersState>(ShowOrdersState.Idle)
    val state: StateFlow<ShowOrdersState> get() = _state


    init {
        showOrders()
    }


    private fun showOrders() {
        viewModelScope.launch {
            showOrdersIntent.consumeAsFlow().collect {
                when (it) {
                    is ShowOrdersIntent.GetOrders -> getOrdersRepo(
                        it.app_version,
                        it.api_token,
                        it.superId,
                    )

                    is ShowOrdersIntent.RejectOrder -> rejectOrderRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                    )

                    is ShowOrdersIntent.CheckCreditLimit -> checkCreditLimitRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                    )

                    is ShowOrdersIntent.CheckQouta -> checkQoutaRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                        it.superId,
                    )

                    is ShowOrdersIntent.ApproveOrder -> approveOrderRepo(
                        it.app_version,
                        it.api_token,
                        it.orderNumber,
                        it.superId,
                    )

                }
            }
        }
    }

    private fun getOrdersRepo(
        app_version: String?,
        api_token: String?,
        superId: String,
    ) {
        viewModelScope.launch {
            _state.value = ShowOrdersState.Loading
            _state.value = try {
                ShowOrdersState.ShowOrders(
                    ShowOrdersRepository().getOrders(
                        app_version,
                        api_token,
                        superId
                    )!!
                )
            } catch (e: Exception) {
                ShowOrdersState.Error(e.message)
            }
        }
    }

    private fun rejectOrderRepo(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
    ) {
        viewModelScope.launch {
            _state.value = ShowOrdersState.Loading
            _state.value = try {
                ShowOrdersState.RejectOrder(
                    ShowOrdersRepository().rejectOrder(
                        app_version,
                        api_token,
                        orderNumber
                    )!!
                )
            } catch (e: Exception) {
                ShowOrdersState.Error(e.message)
            }
        }
    }

    private fun checkCreditLimitRepo(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
    ) {
        viewModelScope.launch {
            _state.value = ShowOrdersState.Loading
            _state.value = try {
                ShowOrdersState.CheckCreditLimit(
                    ShowOrdersRepository().checkCreditLimit(
                        app_version,
                        api_token,
                        orderNumber
                    )!!
                )
            } catch (e: Exception) {
                ShowOrdersState.Error(e.message)
            }
        }
    }

    private fun checkQoutaRepo(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
        super_id: String,
    ) {
        viewModelScope.launch {
            _state.value = ShowOrdersState.Loading
            _state.value = try {
                ShowOrdersState.CheckQouta(
                    ShowOrdersRepository().checkQouta(
                        app_version,
                        api_token,
                        orderNumber,
                        super_id
                    )!!
                )
            } catch (e: Exception) {
                ShowOrdersState.Error(e.message)
            }
        }
    }

    private fun approveOrderRepo(
        app_version: String?,
        api_token: String?,
        orderNumber: String,
        super_id: String,
    ) {
        viewModelScope.launch {
            _state.value = ShowOrdersState.Loading
            _state.value = try {
                ShowOrdersState.ApproveOrder(
                    ShowOrdersRepository().approveOrder(
                        app_version,
                        api_token,
                        orderNumber,
                        super_id
                    )!!
                )
            } catch (e: Exception) {
                ShowOrdersState.Error(e.message)
            }
        }
    }

    companion object {
        private const val TAG = "ShowOrderViewModel"
    }
}