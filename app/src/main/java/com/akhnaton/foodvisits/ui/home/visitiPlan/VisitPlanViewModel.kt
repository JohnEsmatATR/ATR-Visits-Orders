package com.akhnaton.foodvisits.ui.home.visitPlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.model.deleteVisitPlan.DeleteVisitPlanReq
import com.akhnaton.foodvisits.data.model.visitPlan.VisitItem
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitStatus
import com.akhnaton.foodvisits.domin.VisitPlanRepository
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class VisitPlanViewModel : ViewModel() {

    val visitIntent = Channel<VisitIntent>(Channel.UNLIMITED)
    private val _status = MutableStateFlow<VisitStatus?>(null)
    val status: StateFlow<VisitStatus?> = _status

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            visitIntent.consumeAsFlow().collect {
                when (it) {
                    is VisitIntent.GetMonthlyVisits -> getMonthlyVisits()
                    is VisitIntent.UpdateVisitDate -> updateVisitDate(it.id, it.newDate)
                    is VisitIntent.DeleteVisitDate -> deleteVisitDate(it.deleteVisitPlanReq)
                    is VisitIntent.RefreshToken -> refreshToken(it.userId, it.token)
                    is VisitIntent.DeleteVisitPlan -> deleteVisitPlan(it.ids)
                    is VisitIntent.CopyPlan -> copyPlan(it.sourceDate, it.targetDate)
                }
            }
        }
    }

    private fun getMonthlyVisits() {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                val response = VisitPlanRepository().getMonthlyVisits()
                allVisits = response.data.visits
                VisitStatus.GetMonthlyVisits(response)
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }

    private fun updateVisitDate(id: String, newDate: String) {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                val response = VisitPlanRepository().updateVisitDate(id, newDate)
                VisitStatus.UpdateVisitDate(response)
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }

    private fun deleteVisitDate(deleteVisitPlanReq: DeleteVisitPlanReq) {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                val response = VisitRepository().deleteVisitDate(deleteVisitPlanReq)
                VisitStatus.DeleteVisitDate(response)
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                VisitStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }
    private fun deleteVisitPlan(ids: List<Int>) {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                val response = VisitPlanRepository().deleteVisitPlan(ids)
                VisitStatus.DeleteVisitPlan(response)
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }
    private fun copyPlan(sourceDate: String, targetDate: String) {
        viewModelScope.launch {
            _status.value = VisitStatus.Loading
            _status.value = try {
                val response = VisitPlanRepository().copyPlan(sourceDate, targetDate)
                VisitStatus.CopyPlan(response)
            } catch (e: Exception) {
                VisitStatus.Error(e.message)
            }
        }
    }

    private var allVisits: List<VisitItem> = emptyList()

    fun getDaysWithVisits(): Set<String> {
        return allVisits.map { it.start.substring(0, 10) }.toSet()
    }

    fun getVisitsFor(dateKey: String): List<VisitItem> {
        return allVisits.filter { it.start.substring(0, 10) == dateKey }
    }

}