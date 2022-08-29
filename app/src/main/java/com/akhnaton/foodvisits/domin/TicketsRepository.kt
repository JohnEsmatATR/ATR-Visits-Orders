package com.akhnaton.foodvisits.domin

import com.akhnaton.foodvisits.data.interfaces.IChart
import com.akhnaton.foodvisits.data.interfaces.ITickets
import com.akhnaton.foodvisits.shared.RetrofitClient

class TicketsRepository {

    private val retrofit = RetrofitClient.getInstance(ITickets::class.java)

    suspend fun sendTickets(ticketsFun: Int, messages: String, userId: String) =
        retrofit.sendTicket(ticketsFun,messages,userId)
}