package com.akhnaton.foodvisits.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsIntent
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsStatus
import com.akhnaton.foodvisits.domin.TicketsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class TicketsViewModel : ViewModel() {

     val ticketsIntent = Channel<TicketsIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow<TicketsStatus>(TicketsStatus.Idle)

    val state: StateFlow<TicketsStatus> get() = _state


    init {
        sendTicketsData()
    }

    private fun sendTicketsData() {
        viewModelScope.launch {
            ticketsIntent.consumeAsFlow().collect {
                when (it) {
                    is TicketsIntent.Tickets -> sendTicket(
                        it.ticketsFun,
                        it.messages,
                        it.userId,
                    )
                }
            }
        }
    }

    private fun sendTicket(ticketFun: Int, message: String, userId: String) {
        viewModelScope.launch {
            _state.value = TicketsStatus.Loading
            _state.value = try {
                TicketsStatus.SendTickets(
                    TicketsRepository().sendTickets(
                        ticketFun,
                        message,
                        userId
                    )
                )
            } catch (e: Exception) {
                TicketsStatus.Error(e.message)
            }
        }
    }
}