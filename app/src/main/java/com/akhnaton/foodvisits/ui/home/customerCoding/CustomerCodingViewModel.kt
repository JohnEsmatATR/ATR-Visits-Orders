package com.akhnaton.foodvisits.ui.home.customerCoding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.customerCoding.CustomerCodingIntent
import com.akhnaton.foodvisits.data.statusValue.customerCoding.CustomerCodingState
import com.akhnaton.foodvisits.domin.CustomerCodingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class CustomerCodingViewModel : ViewModel() {

    val customerCodingIntent = Channel<CustomerCodingIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<CustomerCodingState>(CustomerCodingState.Idle)

    val state: StateFlow<CustomerCodingState> get() = _state

    init {
        getChartData()
    }

    private fun getChartData() {
        viewModelScope.launch {
            customerCodingIntent.consumeAsFlow().collect {
                when (it) {
                    is CustomerCodingIntent.GetTypes -> getTypes(
                        it.app_version,
                        it.api_token,
                        it.userId,
                    )

                    is CustomerCodingIntent.GetLines -> getLines(
                        it.app_version,
                        it.api_token,
                        it.userId,
                        it.custType,
                    )

                    is CustomerCodingIntent.GetCategories -> getCategories(
                        it.app_version,
                        it.api_token,
                        it.userId,
                        it.custType,
                        it.lineId,
                    )

                    is CustomerCodingIntent.GetAreas -> getAreas(
                        it.app_version,
                        it.api_token,
                        it.userId,
                    )

                    is CustomerCodingIntent.SendCustomer -> sendCustomer(
                        it.app_version,
                        it.api_token,
                        it.user_id,
                        it.cust_type,
                        it.line_id,
                        it.cust_code_id,
                        it.area,
                        it.customer_name,
                        it.customer_address,
                        it.phoneNumber,
                        it.mobileNumber,
                        it.customer_national_id,
                        it.name_in_national_id,
                        it.address_in_national_id,
//                        it.id_1,
//                        it.id_2,
                        it.long,
                        it.lat,
                    )
                }
            }
        }
    }


    private fun getTypes(
        appVersion: String,
        apiToken: String,
        userId: String,
    ) {
        viewModelScope.launch {
            _state.value = CustomerCodingState.Loading
            _state.value = try {
                CustomerCodingState.GetTypes(
                    CustomerCodingRepository().getTypes(
                        appVersion,
                        apiToken,
                        userId,
                    )
                )
            } catch (e: Exception) {
                CustomerCodingState.Error(e.message)
            }
        }
    }

    private fun getLines(
        appVersion: String,
        apiToken: String,
        userId: String,
        custType: String,
    ) {
        viewModelScope.launch {
            _state.value = CustomerCodingState.Loading
            _state.value = try {
                CustomerCodingState.GetLines(
                    CustomerCodingRepository().getLines(
                        appVersion,
                        apiToken,
                        userId,
                        custType,
                    )
                )
            } catch (e: Exception) {
                CustomerCodingState.Error(e.message)
            }
        }
    }

    private fun getCategories(
        appVersion: String,
        apiToken: String,
        userId: String,
        custType: String,
        lineId: String,
    ) {
        viewModelScope.launch {
            _state.value = CustomerCodingState.Loading
            _state.value = try {
                CustomerCodingState.GetCategories(
                    CustomerCodingRepository().getCategories(
                        appVersion,
                        apiToken,
                        userId,
                        custType,
                        lineId,
                    )
                )
            } catch (e: Exception) {
                CustomerCodingState.Error(e.message)
            }
        }
    }

    private fun getAreas(
        appVersion: String,
        apiToken: String,
        userId: String,
    ) {
        viewModelScope.launch {
            _state.value = CustomerCodingState.Loading
            _state.value = try {
                CustomerCodingState.GetAreas(
                    CustomerCodingRepository().getAreas(
                        appVersion,
                        apiToken,
                        userId,
                    )
                )
            } catch (e: Exception) {
                CustomerCodingState.Error(e.message)
            }
        }
    }

    private fun sendCustomer(
        appVersion: RequestBody,
        apiToken: RequestBody,
        userId: RequestBody,
        custType: RequestBody,
        lineId: RequestBody,
        categoryId: RequestBody,
        area: RequestBody,
        customerName: RequestBody,
        customerAddress: RequestBody,
        phoneNumber: RequestBody,
        mobileNumber: RequestBody,
        customerNationalId: RequestBody,
        nameInNationalId: RequestBody,
        addressInNationalId: RequestBody,
//        id_1: MultipartBody.Part,
//        id_2: MultipartBody.Part,
        long: RequestBody,
        lat: RequestBody,
    ) {
        viewModelScope.launch {
            _state.value = CustomerCodingState.Loading
            _state.value = try {
                CustomerCodingState.SendCustomer(
                    CustomerCodingRepository().sendCustomer(
                        appVersion,
                        apiToken,
                        userId,
                        custType,
                        lineId,
                        categoryId,
                        area,
                        customerName,
                        customerAddress,
                        phoneNumber,
                        mobileNumber,
                        customerNationalId,
                        nameInNationalId,
                        addressInNationalId,
//                        id_1,
//                        id_2,
                        long,
                        lat,
                    )
                )
            } catch (e: Exception) {
                CustomerCodingState.Error(e.message)
            }
        }
    }
}