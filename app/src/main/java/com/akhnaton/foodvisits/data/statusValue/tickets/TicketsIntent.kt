package com.akhnaton.foodvisits.data.statusValue.tickets

sealed class TicketsIntent {
    data class Tickets(
        val ticketsFun: Int, val messages: String, val userId: String
    ) : TicketsIntent()
}