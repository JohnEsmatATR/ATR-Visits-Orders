package com.akhnaton.foodvisits.ui.home.visits.promoters.promoterDayDetails

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

class DayDetailsViewModel : ViewModel() {
    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        sendDetails()
    }

    private fun sendDetails() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.SendDetails -> fetchSendCalls(
                        it.appVersion,
                        it.apiToken,
                        it.createdBy,
                        it.creationDate,
                        it.partySite,
                        it.customerCode,
                        it.customerAvg,
                        it.customerCall,
                        it.customerPositiveCall,
                        it.customerPurchase,
                        it.userType,
                        it.StockDayDetails,
                    )

                    else -> {}
                }
            }
        }
    }

    private fun fetchSendCalls(
        appVersion: Double,
        apiToken: String,
        createdBy: Int,
        creationDate: String,
        partySiteId: Int,
        customerCode: Int,
        customerAvg: Int,
        customerCalls: Int,
        customerPositiveCalls: Int,
        customerPurchased: Int,
        userType: String,
        StockDayDetails: Int,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.SendDetails(
                    PromoterRepository().sendDetails(
                        appVersion,
                        apiToken,
                        createdBy,
                        creationDate,
                        partySiteId,
                        customerCode,
                        customerAvg,
                        customerCalls,
                        customerPositiveCalls,
                        customerPurchased,
                        userType,
                        StockDayDetails,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }
}