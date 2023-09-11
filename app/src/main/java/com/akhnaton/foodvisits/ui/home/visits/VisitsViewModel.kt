package com.akhnaton.foodvisits.ui.home.visits

import android.content.Context
import android.util.Log
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

class VisitsViewModel(val context: Context) : ViewModel() {

    val visitsIntent = Channel<VisitsIntent>(Channel.UNLIMITED)

    private val _statusVisit = MutableStateFlow<VisitsStatus>(VisitsStatus.Idle)
    val statusVisit: StateFlow<VisitsStatus> get() = _statusVisit

    init {
        getPlan()
    }

    private fun getPlan() {
        viewModelScope.launch {
            visitsIntent.consumeAsFlow().collect {
                when (it) {
                    is VisitsIntent.GetPlan -> fetchPlan(it.version, it.token)
                    is VisitsIntent.SaveVisit -> fetchSaveVisit(
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
                    )
                    is VisitsIntent.SaveVisitOnline -> saveVisitOnline()
                    is VisitsIntent.GetAppSetting -> getAppSetting(it.app_version)
                }
            }
        }
    }

    private fun fetchPlan(version: String, token: String) {
        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.Plan(VisitsRepository(context).getPlan(version, token))
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }

    private fun fetchSaveVisit(
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
    ) {

        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.SaveVisits(
                    VisitsRepository(context).saveVisit(
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
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }


    private fun saveVisitOnline() {

        viewModelScope.launch {
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.SaveVisitsOnline(
                    VisitsRepository(context).saveVisitOnline()
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
            _statusVisit.value = VisitsStatus.Loading
            _statusVisit.value = try {
                VisitsStatus.GetAppSetting(
                    VisitsRepository(context).getAppSetting(
                        appVersion
                    )
                )
            } catch (e: Exception) {
                VisitsStatus.Error(e.message)
            }
        }
    }
}