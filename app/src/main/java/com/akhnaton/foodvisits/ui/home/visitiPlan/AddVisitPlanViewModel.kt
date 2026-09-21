package com.akhnaton.foodvisits.ui.home.visitPlan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.copyDayPlan.CopyDayPlanReq
import com.akhnaton.foodvisits.data.model.visitPlan.SaveSetupPlanRequest
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitStatus
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Intent
import com.akhnaton.foodvisits.data.statusValue.visits2.Visits2Status
import com.akhnaton.foodvisits.domin.AddVisitRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import com.akhnaton.foodvisits.domin.Visits2Repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class AddVisitPlanViewModel : ViewModel() {

    val addVisitPlanIntent = Channel<AddVisitIntent>(Channel.UNLIMITED)
    private val _status = MutableStateFlow<AddVisitStatus?>(null)
    val status: StateFlow<AddVisitStatus?> = _status

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            addVisitPlanIntent.consumeAsFlow().collect {
                when (it) {
                    is AddVisitIntent.GetSalesTypes -> getSalesTypes()
                    is AddVisitIntent.GetLines -> getLines(it.saleType)
                    is AddVisitIntent.GetCustomers -> getCustomers(it.lineId)
                    is AddVisitIntent.SaveSetupPlan -> saveSetupPlan(it.request)
                    is AddVisitIntent.GetSalesMan -> getSalesMan()
                    is AddVisitIntent.CopyDayPlan -> copyDayPlan(it.copyDayPlanReq)
                    is AddVisitIntent.RefreshToken -> refreshToken(it.userId, it.token)
                }
            }
        }
    }

    private fun getSalesTypes() {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                val response = AddVisitRepository().getSalesAndCustomerTypes()
                AddVisitStatus.GetSalesTypes(response)
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun getLines(saleType: String) {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                val response = AddVisitRepository().getLines(saleType)
                AddVisitStatus.GetLines(response)
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun getCustomers(lineId: String) {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                val response = AddVisitRepository().getVisitCustomers(lineId)
                AddVisitStatus.GetCustomers(response)
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun saveSetupPlan(request: SaveSetupPlanRequest) {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                val response = AddVisitRepository().saveSetupPlan(request)
                AddVisitStatus.SaveSetupPlan(response)
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun getSalesMan() {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                AddVisitStatus.GetSalesMan(
                    AddVisitRepository().getSalesMan()
                )
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun copyDayPlan(copyDayPlanReq: CopyDayPlanReq) {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                AddVisitStatus.CopyDayPlan(
                    AddVisitRepository().copyDayPlan(copyDayPlanReq)
                )
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                AddVisitStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

}