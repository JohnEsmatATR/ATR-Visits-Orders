package com.akhnaton.foodvisits.ui.home.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.domin.VisitsRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
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
                        it.dateVisit,
                        it.phoneVisit
                    )
                    is VisitsIntent.GetAppSetting -> getAppSetting(it.app_version)
                }
            }
        }
    }

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _status.value = VisitsStatus.Loading
            _status.value = try {
                VisitsStatus.Plan(VisitsRepository().getPlan(version, token))
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
        dateVisit: String,
        phoneVisit: Boolean
    ) {

        viewModelScope.launch {
            _status.value = VisitsStatus.Loading
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
                        dateVisit,
                        phoneVisit
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }


    private fun getAppSetting(
        appVersion: String
    ) {
        viewModelScope.launch {
            _status.value = VisitsStatus.Loading
            _status.value = try {
                VisitsStatus.GetAppSetting(
                    VisitsRepository().getAppSetting(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }
}