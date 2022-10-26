package com.akhnaton.foodvisits.data.statusValue.tickets

import com.akhnaton.foodvisits.data.model.tickets.Tickets

sealed class TicketsStatus {

    object Idle : TicketsStatus()
    object Loading : TicketsStatus()
    data class SendTickets(val data: Tickets) : TicketsStatus()
    data class Error(val error: String?) : TicketsStatus()
}