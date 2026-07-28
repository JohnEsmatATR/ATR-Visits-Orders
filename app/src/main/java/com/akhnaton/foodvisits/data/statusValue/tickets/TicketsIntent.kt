package com.akhnaton.foodvisits.data.statusValue.tickets

import okhttp3.MultipartBody
import okhttp3.RequestBody

sealed class TicketsIntent {
    data class Tickets(
        val ticketsFun: String, val messages: String, val userId: String
    ) : TicketsIntent()

    object GetAllUsers : TicketsIntent()

    data class CreateTicket(
        val phone: RequestBody,
        val cc: RequestBody,
        val subtitle: RequestBody,
        val description: RequestBody,
        val files: List<MultipartBody.Part>?
    ) : TicketsIntent()
}