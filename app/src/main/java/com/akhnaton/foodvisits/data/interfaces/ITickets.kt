package com.akhnaton.foodvisits.data.interfaces

import com.akhnaton.foodvisits.data.model.tickets.Tickets
import com.akhnaton.foodvisits.shared.ConstantLinks.TICKET_SYSTEM
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ITickets {

    @FormUrlEncoded
    @POST(TICKET_SYSTEM)
    suspend fun sendTicket(
        @Field("app_version") ticketSystem: String,
        @Field("api_token") userId: String?,
        @Field("messages") messages: String?
    ): Tickets
}