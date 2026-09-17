package com.akhnaton.foodvisits.ui.home.visitPlan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitStatus
import com.akhnaton.foodvisits.domin.AddVisitPlanRepository
import com.akhnaton.foodvisits.domin.LinesRepository
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
                }
            }
        }
    }

    private fun getSalesTypes() {
        viewModelScope.launch {
            _status.value = AddVisitStatus.Loading
            _status.value = try {
                val response = AddVisitPlanRepository().getSalesAndCustomerTypes()
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
                val response = LinesRepository().getLines(saleType)
                AddVisitStatus.GetLines(response)
            } catch (e: Exception) {
                AddVisitStatus.Error(e.message)
            }
        }
    }

}