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

class PromoterGetItemsViewModel : ViewModel() {

    val promoterIntent = Channel<PromoterIntent>(Channel.UNLIMITED)

    private val _status = MutableStateFlow<PromoterStatus>(PromoterStatus.Idle)

    val status: StateFlow<PromoterStatus> get() = _status

    init {
        getCurrentStockItems()
        submitItems()
    }


    private fun getCurrentStockItems() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.GetCurrentStockItems -> fetchGetCurrentStockItems(
                        it.appVersion,
                        it.apiToken,
                        it.createdBy,
                        it.partySiteId,
                        it.customerCode,
                        it.creationDate,
                        it.items,
                    )

                    else -> {}
                }
            }
        }
    }

    private fun fetchGetCurrentStockItems(
        appVersion: Double,
        apiToken: String,
        createdBy: Int,
        partySiteId: Int,
        customerCode: Int,
        creationDate: String,
        items: Int,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.GetCurrentStockItems(
                    PromoterRepository().getCurrentStockItems(
                        appVersion,
                        apiToken,
                        createdBy,
                        partySiteId,
                        customerCode,
                        creationDate,
                        items,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }


    private fun fetchSubmitItems(
        appVersion: Double?,
        apiToken: String?,
        createdBy: Int?,
        partySite: Int?,
        date: String?,
        itemId: Int?,
        returnQuantity: Int?,
        quantity: Int?,
        price: Double?,
        customerCode: Int?,
        userType: String?,
    ) {
        viewModelScope.launch {
            _status.value = PromoterStatus.Loading
            _status.value = try {
                PromoterStatus.SubmitItems(
                    PromoterRepository().submitItems(
                        appVersion,
                        apiToken,
                        createdBy,
                        partySite,
                        date,
                        itemId,
                        returnQuantity,
                        quantity,
                        price,
                        customerCode,
                        userType,
                    )
                )
            } catch (e: Exception) {
                PromoterStatus.Error(e.message)
            }
        }
    }


    private fun submitItems() {
        viewModelScope.launch {
            promoterIntent.consumeAsFlow().collect {
                when (it) {
                    is PromoterIntent.SubmitStock -> fetchSubmitItems(
                        it.appVersion,
                        it.apiToken,
                        it.createdBy,
                        it.partySite,
                        it.date,
                        it.itemId,
                        it.returnQuantity,
                        it.quantity,
                        it.price,
                        it.customerCode,
                        it.userType,
                    )

                    else -> {}
                }
            }
        }
    }
}