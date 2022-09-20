package com.akhnaton.foodvisits.ui.phoneVisit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class PhoneVisitsViewModel : ViewModel() {


    val phoneVisitsIntent = Channel<PhoneVisitsIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PhoneVisitsStatus>(PhoneVisitsStatus.Idle)

    val status: StateFlow<PhoneVisitsStatus> get() = _status

    init {
        getPlan()
    }

    private fun getPlan() {
        viewModelScope.launch {
            phoneVisitsIntent.consumeAsFlow().collect {
                when (it) {
                    is PhoneVisitsIntent.GetPlan -> fetchPlan(it.version, it.token)
                    is PhoneVisitsIntent.GetCustomerType -> fetchCustomerType(it.version, it.token)
                    is PhoneVisitsIntent.GetLines -> fetchLines(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType
                    )
                    is PhoneVisitsIntent.GetCustomerLines -> fetchCustomerLine(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId
                    )

                    is PhoneVisitsIntent.GetCustomersSite -> fetchCustomersSite(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId,
                        it.customerCode
                    )
                    is PhoneVisitsIntent.SaveVisit -> saveVisit(
                        it.version,
                        it.token,
                        it.customerPartySiteId,
                        it.visitType,
                        it.visitTarget,
                        it.visitActualTarget,
                        it.latitude,
                        it.longtitude,
                        it.deviceType,
                        it.zoneFlag,
                        it.checkInDate,
                        it.dateVisit
                    )
                    is PhoneVisitsIntent.GetOrderLimit -> getLimitArea(it.app_version)
                }
            }
        }
    }

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.Plan(PhoneVisitsRepository().getPlan(version, token))
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchCustomerType(version: String, token: String) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.GetCustomerType(
                    PhoneVisitsRepository().getCustomerType(
                        version,
                        token
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
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
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.GetLines(
                    PhoneVisitsRepository().getLines(
                        version,
                        token,
                        customerType,
                        orderType
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }


    private fun fetchCustomerLine(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        linesId: String
    ) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.GetCustomerLines(
                    PhoneVisitsRepository().getMainLineCustomer(
                        version,
                        token,
                        customerType,
                        orderType,
                        linesId
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchCustomersSite(
        version: String,
        token: String,
        customerType: String,
        orderType: String,
        lineId: String,
        customer_code: String
    ) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.GetCustomersSite(
                    PhoneVisitsRepository().getCustomersSite(
                        version,
                        token,
                        customerType,
                        orderType,
                        lineId,
                        customer_code
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun saveVisit(
        version: String,
        token: String,
        customerPartySiteId: String,
        visitType: String,
        visitTarget: String,
        visitActualTarget: String,
        latitude: String,
        longitude: String,
        deviceType: String,
        zoneFlag: String,
        checkInDate: String,
        dateVisit: String
    ) {

        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.SavePhoneVisits(
                    PhoneVisitsRepository().saveVisit(
                        version,
                        token,
                        customerPartySiteId,
                        visitType,
                        visitTarget,
                        visitActualTarget,
                        latitude,
                        longitude,
                        deviceType,
                        zoneFlag,
                        checkInDate,
                        dateVisit
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }

    }

    private fun getLimitArea(
        appVersion: String
    ) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Idle
            _status.value = try {
                PhoneVisitsStatus.GetLimitArea(
                    PhoneVisitsRepository().getAreaLimit(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }
}


