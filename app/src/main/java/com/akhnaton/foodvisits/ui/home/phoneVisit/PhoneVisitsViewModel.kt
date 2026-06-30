package com.akhnaton.foodvisits.ui.home.phoneVisit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.saveVisitPhone.SaveVisitPhoneReq
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
                    is PhoneVisitsIntent.GetSalesAndCustomerTypes -> getSalesAndCustomerTypes()
                    is PhoneVisitsIntent.GetCustomers -> getCustomers(it.saleType)
                    is PhoneVisitsIntent.GetCustomerData -> getCustomerData(
                        it.saleType,
                        it.customerCode,
                        it.line
                    )
                    is PhoneVisitsIntent.VisitsSelect -> visitsSelect(
                        it.orderType,
                        it.customerCode
                    )

                    is PhoneVisitsIntent.SaveVisitPhone -> saveVisitPhone(
                        it.saveVisitPhoneReq,
                    )

                    is PhoneVisitsIntent.RefreshToken -> refreshToken(it.userId, it.token)

                    //------------------------------------------------------------------------------

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
                        it.dateVisit,
                        it.customerType,
                        it.orderType,
                        it.phoneVisit
                    )

                    is PhoneVisitsIntent.GetAppSetting -> getAppSetting(it.app_version)
                }
            }
        }
    }

    private fun getSalesAndCustomerTypes() {
        Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL1")
                PhoneVisitsStatus.GetSalesAndCustomerTypes(
                    PhoneVisitsRepository().getSalesAndCustomerTypes()
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getSalesAndCustomerTypesVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun getCustomers(saleType: String) {
        Log.d("WHAT", "getCustomersVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "getCustomersVIEWMODEL1")
                PhoneVisitsStatus.GetCustomers(
                    PhoneVisitsRepository().getCustomers(saleType)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getCustomersVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun getCustomerData(saleType: String, customerCode: String, line: String) {
        Log.d("WHAT", "getCustomersVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "getCustomersVIEWMODEL1")
                PhoneVisitsStatus.GetCustomerData(
                    PhoneVisitsRepository().getCustomerData(saleType, customerCode, line)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "getCustomerDataVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun visitsSelect(orderType: String, customerCode: String) {
        Log.d("WHAT", "visitsSelectVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "visitsSelectVIEWMODEL1")
                PhoneVisitsStatus.VisitsSelect(
                    PhoneVisitsRepository().visitsSelect(orderType, customerCode)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "visitsSelectVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun saveVisitPhone(saveVisitPhoneReq: SaveVisitPhoneReq) {
        Log.d("WHAT", "getCustomersVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "getCustomersVIEWMODEL1")
                PhoneVisitsStatus.SaveVisitPhone(
                    PhoneVisitsRepository().saveVisitPhone(saveVisitPhoneReq)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "saveVisitPhoneVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        Log.d("WHAT", "refreshTokenVIEWMODEL")
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                Log.d("WHAT", "refreshTokenVIEWMODEL1")
                PhoneVisitsStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                Log.d("WHAT", "refreshTokenVIEWMODEL2 ${e.message}")
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                PhoneVisitsStatus.Plan(PhoneVisitsRepository().getPlan(version, token))
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchCustomerType(version: String, token: String) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
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
            _status.value = PhoneVisitsStatus.Loading
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
            _status.value = PhoneVisitsStatus.Loading
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
            _status.value = PhoneVisitsStatus.Loading
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
        dateVisit: String,
        customerType: String,
        orderType: String,
        phoneVisit: Boolean
    ) {

        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
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
                        dateVisit,
                        customerType,
                        orderType,
                        phoneVisit
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }

    }

    private fun getAppSetting(
        appVersion: String
    ) {
        viewModelScope.launch {
            _status.value = PhoneVisitsStatus.Loading
            _status.value = try {
                PhoneVisitsStatus.GetAppSetting(
                    PhoneVisitsRepository().getAppSetting(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                PhoneVisitsStatus.Error(e.message)
            }
        }
    }
}


