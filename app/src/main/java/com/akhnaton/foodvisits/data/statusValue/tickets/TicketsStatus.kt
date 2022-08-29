package com.akhnaton.foodvisits.data.statusValue.tickets

sealed class TicketsStatus {

    object Idle : TicketsStatus()
    object Loading : TicketsStatus()
    data class SendTickets(val data: String) : TicketsStatus()
    data class Error(val error: String?) : TicketsStatus()
}