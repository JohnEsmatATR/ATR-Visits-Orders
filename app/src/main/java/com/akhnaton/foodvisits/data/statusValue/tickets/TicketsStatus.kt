package com.akhnaton.foodvisits.data.statusValue.tickets

import com.akhnaton.foodvisits.data.model.CreateTicketRes
import com.akhnaton.foodvisits.data.model.getAllUsers.GetAllUsersRes
import com.akhnaton.foodvisits.data.model.tickets.Tickets

sealed class TicketsStatus {

    object Idle : TicketsStatus()
    object Loading : TicketsStatus()
    data class SendTickets(val data: Tickets) : TicketsStatus()
    data class GetAllUsers(val data: GetAllUsersRes) : TicketsStatus()
    data class CreateTicket(val data: CreateTicketRes) : TicketsStatus()
    data class Error(val error: String?) : TicketsStatus()
}