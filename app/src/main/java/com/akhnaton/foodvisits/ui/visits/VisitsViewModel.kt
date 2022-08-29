package com.akhnaton.foodvisits.ui.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.domin.VisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class VisitsViewModel : ViewModel() {


    val visitsIntent = Channel<VisitsIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<VisitsStatus>(VisitsStatus.Idle)

    val status: StateFlow<VisitsStatus> get() = _status

    init {
        getPlan()
    }

    private fun getPlan() {
        viewModelScope.launch {
            visitsIntent.consumeAsFlow().collect {
                when (it) {
                    is VisitsIntent.GetPlan -> fetchPlan(it.version, it.token)
                    is VisitsIntent.GetCustomerType -> fetchCustomerType(it.version, it.token)
                    is VisitsIntent.GetLines -> fetchLines(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType
                    )
                    is VisitsIntent.GetCustomerLines -> fetchCustomerLine(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId
                    )

                    is VisitsIntent.GetCustomersSite -> fetchCustomersSite(
                        it.version,
                        it.token,
                        it.customerType,
                        it.orderType,
                        it.lineId,
                        it.customerCode
                    )
                    is VisitsIntent.SaveVisit -> saveVisit(
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
                }
            }
        }
    }

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.Plan(VisitsRepository().getPlan(version, token))
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchCustomerType(version: String, token: String) {
        viewModelScope.launch {
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.GetCustomerType(VisitsRepository().getCustomerType(version, token))
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
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
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.GetLines(
                    VisitsRepository().getLines(
                        version,
                        token,
                        customerType,
                        orderType
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
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
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.GetCustomerLines(
                    VisitsRepository().getMainLineCustomer(
                        version,
                        token,
                        customerType,
                        orderType,
                        linesId
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
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
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.GetCustomersSite(
                    VisitsRepository().getCustomersSite(
                        version,
                        token,
                        customerType,
                        orderType,
                        lineId,
                        customer_code
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
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
            _status.value = VisitsStatus.Idle
            _status.value = try {
                VisitsStatus.SaveVisits(
                    VisitsRepository().saveVisit(
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
                VisitsStatus.Error(e.message)

            }
        }

    }
}