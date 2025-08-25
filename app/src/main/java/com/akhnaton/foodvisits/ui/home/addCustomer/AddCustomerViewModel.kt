package com.akhnaton.foodvisits.ui.home.addCustomer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerIntent
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerStatus
import com.akhnaton.foodvisits.domin.AddCustomerRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class AddCustomerViewModel : ViewModel() {

    val customerIntent = Channel<AddCustomerIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<AddCustomerStatus>(AddCustomerStatus.Idle)

    val state: StateFlow<AddCustomerStatus> get() = _state


    init {
        sendTicketsData()
    }

    private fun sendTicketsData() {
        viewModelScope.launch {
            customerIntent.consumeAsFlow().collect {
                when (it) {
                    is AddCustomerIntent.GetCustomerType -> fetchCustomerType(it.version, it.token)
                    is AddCustomerIntent.GetLines -> fetchLines(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType
                    )

                    is AddCustomerIntent.GetMainLines -> fetchMainLine(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId
                    )

                    is AddCustomerIntent.CreateCustomer -> createCustomer(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId,
                        it.customerCode,
                        it.customerName,
                        it.customerAddress,
                        it.nationalId,
                        it.latitude,
                        it.longitude
                    )
                }
            }
        }
    }

    private fun fetchCustomerType(version: String, token: String) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Idle
            _state.value = try {
                AddCustomerStatus.GetCustomerType(
                    PhoneVisitsRepository().getCustomerType(
                        version,
                        token
                    )
                )
            } catch (e: Exception) {
                AddCustomerStatus.Error(e.message)
            }
        }
    }


    private fun fetchLines(
        version: String,
        token: String,
        customerType: String,
        orderType: String
    ) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Idle
            _state.value = try {
                AddCustomerStatus.GetLines(
                    PhoneVisitsRepository().getLines(
                        version,
                        token,
                        customerType,
                        orderType
                    )
                )
            } catch (e: Exception) {
                AddCustomerStatus.Error(e.message)
            }
        }
    }

    private fun fetchMainLine(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        linesId: String
    ) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Idle
            _state.value = try {
                AddCustomerStatus.GetMainLine(
                    AddCustomerRepository().getMainLineCustomer(
                        version,
                        token,
                        customerType,
                        orderType,
                        linesId
                    )
                )
            } catch (e: Exception) {
                AddCustomerStatus.Error(e.message)
            }
        }
    }

    private fun createCustomer(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String,
        customerCode: String,
        customerName: String,
        customerAddress: String,
        nationalId: String,
        latitude: String,
        longitude: String,
    ) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Idle
            _state.value = try {
                AddCustomerStatus.CreateCustomer(
                    AddCustomerRepository().createNewCustomer(
                        version,
                        token,
                        customerType,
                        orderType,
                        lineId,
                        customerCode,
                        customerName,
                        customerAddress,
                        nationalId,
                        latitude,
                        longitude
                    )
                )
            } catch (e: Exception) {
                AddCustomerStatus.Error(e.message)
            }
        }
    }
}