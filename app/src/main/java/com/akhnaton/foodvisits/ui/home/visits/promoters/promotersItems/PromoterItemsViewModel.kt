package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.domin.PromoterRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class PromoterItemsViewModel : ViewModel() {

    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        getItems()
        getCurrentStockItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.GetItems -> fetchGetItems(
                        it.items,
                        it.createdBy,
                        it.customerCode,
                        it.partySiteId
                    )
                }
            }
        }
    }

    private fun fetchGetItems(
        items: String,
        createdBy: String,
        customerCode: String,
        partySiteId: String,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.GetItems(
                    PromoterRepository().getItems(
                        items,
                        createdBy,
                        customerCode,
                        partySiteId
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }

    private fun getCurrentStockItems() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.GetCurrentStockItems -> fetchGetCurrentStockItems(
                        it.employeeId,
                        it.partySite,
                        it.code,
                        it.creationDate,
                        it.userType,
                        it.funNum,
                    )
                }
            }
        }
    }

    private fun fetchGetCurrentStockItems(
        employeeId: String,
        partySite: String,
        code: String,
        creationDate: String,
        userType: String,
        funNum: String,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.GetCurrentStockItems(
                    PromoterRepository().getCurrentStockItems(
                        employeeId,
                        partySite,
                        code,
                        creationDate,
                        userType,
                        funNum,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }
}