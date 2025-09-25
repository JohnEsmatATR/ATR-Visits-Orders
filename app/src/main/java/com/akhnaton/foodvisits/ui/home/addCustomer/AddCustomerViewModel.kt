package com.akhnaton.foodvisits.ui.home.addCustomer

import AddCustomerStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerIntent
import com.akhnaton.foodvisits.domin.AddCustomerRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

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
                        it.governorate,
                        it.city,
                        it.customerName,
                        it.phone,
                        it.secondPhone,
                        it.customerAddress,
                        it.nationalId,
                        it.latitude,
                        it.longitude,
                        it.suggetsAddress,
                        it.id_1,
                        it.id_2
                    )
                }
            }
        }
    }

    private fun fetchCustomerType(version: String, token: String) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Loading
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
            _state.value = AddCustomerStatus.Loading
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
            _state.value = AddCustomerStatus.Loading
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
        version: RequestBody,
        token: RequestBody,
        customerType: RequestBody,
        orderType: RequestBody,
        lineId: RequestBody,
        governorate: RequestBody,
        city: RequestBody,
        customerName: RequestBody,
        phone : RequestBody,
        secondPhone : RequestBody,
        customerAddress: RequestBody,
        nationalId: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        suggetsAddress : RequestBody,
        id_1: MultipartBody.Part,
        id_2: MultipartBody.Part,
    ) {
        viewModelScope.launch {
            _state.value = AddCustomerStatus.Loading
            _state.value = try {
                AddCustomerStatus.CreateCustomer(
                    AddCustomerRepository().createNewCustomer(
                        version,
                        token,
                        customerType,
                        orderType,
                        lineId,
                        governorate,
                        city,
                        customerName,
                        phone,
                        secondPhone,
                        customerAddress,
                        nationalId,
                        latitude,
                        longitude,
                        suggetsAddress,
                        id_1,
                        id_2
                    )
                )
            } catch (e: Exception) {
                AddCustomerStatus.Error(e.message)
            }
        }
    }
}