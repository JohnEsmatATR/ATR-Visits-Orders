package com.akhnaton.foodvisits.ui.home.CardPrint

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

}