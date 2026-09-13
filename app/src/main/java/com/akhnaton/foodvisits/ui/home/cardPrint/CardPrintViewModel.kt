package com.akhnaton.foodvisits.ui.home.cardPrint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintIntent
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintStatus
import com.akhnaton.foodvisits.domin.CardPrintRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import com.akhnaton.foodvisits.domin.PhoneVisitsRepository

class CardPrintViewModel : ViewModel() {

    val cardPrintIntent = Channel<CardPrintIntent>(Channel.UNLIMITED)
    private val _status = MutableStateFlow<CardPrintStatus?>(null)
    val status: StateFlow<CardPrintStatus?> = _status

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            cardPrintIntent.consumeAsFlow().collect {
                when (it) {
                    is CardPrintIntent.GetPrintInvoicesList -> getPrintInvoicesList()
                    is CardPrintIntent.GetPrintInvoiceDetails -> getPrintInvoiceDetails(it.orderSalesNumber)
                    is CardPrintIntent.RefreshToken -> refreshToken(it.userId, it.token)
                }
            }
        }
    }

    private fun getPrintInvoicesList() {
        viewModelScope.launch {
            _status.value = CardPrintStatus.Loading
            _status.value = try {
                CardPrintStatus.GetPrintInvoicesList(
                    CardPrintRepository().getPrintInvoicesList()
                )
            } catch (e: Exception) {
                CardPrintStatus.Error(e.message)
            }
        }
    }

    private fun getPrintInvoiceDetails(orderSalesNumber: String) {
        viewModelScope.launch {
            _status.value = CardPrintStatus.Loading
            _status.value = try {
                CardPrintStatus.GetPrintInvoiceDetails(
                    CardPrintRepository().getPrintInvoiceDetails(orderSalesNumber)
                )
            } catch (e: Exception) {
                CardPrintStatus.Error(e.message)
            }
        }
    }

    private fun refreshToken(userId: String, token: String) {
        viewModelScope.launch {
            _status.value = CardPrintStatus.Loading
            _status.value = try {
                CardPrintStatus.RefreshToken(
                    PhoneVisitsRepository().refreshToken(userId, token)
                )
            } catch (e: Exception) {
                CardPrintStatus.Error(e.message)
            }
        }
    }

}